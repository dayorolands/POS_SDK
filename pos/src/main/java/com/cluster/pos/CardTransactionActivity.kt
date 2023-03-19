package com.cluster.pos

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.window.SplashScreen
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.cluster.core.data.api.retrofitService
import com.cluster.core.util.*
import com.cluster.pos.api.PosApiService
import com.cluster.pos.card.*
import com.cluster.pos.data.PosPreferences
import com.cluster.pos.data.create
import com.cluster.pos.extension.*
import com.cluster.pos.helpers.IsoSocketHelper
import com.cluster.pos.model.ConnectionInfo
import com.cluster.pos.model.getSupportedRoute
import com.cluster.pos.models.FinancialTransaction
import com.cluster.pos.models.PosNotification
import com.cluster.pos.models.PosTransaction
import com.cluster.pos.models.Reversal
import com.cluster.pos.printer.PrinterStatus
import com.cluster.pos.printer.posReceipt
import com.cluster.pos.service.CallHomeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jpos.iso.ISOMsg
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.net.ConnectException
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("Registered")
abstract class CardTransactionActivity : PosActivity() {
    private val posManager: PosManager by inject { parametersOf(this) }
    internal val sessionData: PosManager.SessionData get() = posManager.sessionData
    internal val viewModel: PosTransactionViewModel by viewModels()
    internal var previousMessage: ISOMsg? = null
    abstract var transactionType: TransactionType
    private lateinit var cardData: CardData
    private val posApiService: PosApiService by retrofitService()
    private val posPreferences: PosPreferences by inject()
    private val callHomeService: CallHomeService by inject()
    lateinit var posParameter: PosParameter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        posParameter = get()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }

        registerReceiver(screenStateReceiver, filter)
        viewModel.amount.observe(this) {
            viewModel.amountCurrencyFormat.value = it?.toCurrencyFormat()
            viewModel.amountNumberFormat.value = it?.format()
        }
        viewModel.amountString.observe(this) {
            viewModel.longAmount.value = it?.toLongOrNull()
            viewModel.amount.value = (it?.toDoubleOrNull() ?: 0.0) / 100
        }

        checkParameters()
    }

    abstract fun onPosReady()

    private fun checkParameters() {
        sessionData.getDukptConfig = { pan, amount ->
            posPreferences.binRoutes?.getSupportedRoute(pan, amount)?.dukptConfig
        }
        sessionData.getPosParameter = ::getPosParameter
        onPosReady()
    }

    private fun finishWithError(message: String) {
        dialogProvider.showError(message) {
            onClose {
                finish()
            }
        }
    }

    fun requestCard() {
        mainScope.launch {
            dialogProvider.showProgressBar("Loading card functions")
            posManager.loadEmv()
            delay(1000)
            dialogProvider.hideProgressBar()

            if (Platform.hasPrinter) {
                val printerStatus = withContext(Dispatchers.Default) { printer.check() }
                if (printerStatus != PrinterStatus.READY) {
                    dialogProvider.showErrorAndWait(printerStatus.message)
                    finish()
                    return@launch
                }
            }

            when (val cardEvent = posManager.cardReader.waitForCard()) {
                CardReaderEvent.REMOVED, CardReaderEvent.CANCELLED -> {
                    stopTimer()
                    finish()
                    return@launch
                }

                CardReaderEvent.CHIP_FAILURE -> {
                    stopTimer()
                    renderTransactionFailure("Please Remove Card", "")
                    return@launch
                }

                CardReaderEvent.Timeout -> {
                    stopTimer()
                    renderTransactionFailure("Device timeout due to inactivity", "")
                    return@launch
                }

                else -> {
                    viewModel.cardReaderEvent.value = cardEvent
                    restartTimer()

                    showSelectAccountScreen { accountType ->
                        viewModel.accountType.value = accountType
                        onSelectAccountType()
                    }
                }
            }
        }
    }

    private var sessionTimer: TimerTask? = null

    internal fun restartTimer() {
        if (sessionTimer != null) {
            sessionTimer?.cancel()
            sessionTimer = null
        }
        sessionTimer = Timer().schedule(60000) {
            runOnUiThread {
                renderTransactionFailure("Timeout due to card holder inactivity")
                stopTimer()
            }
        }
    }

    private fun stopTimer() {
        sessionTimer?.cancel()
        sessionTimer = null
    }

    override fun onDestroy() {
        safeRun {
            stopTimer()
            unregisterReceiver(screenStateReceiver)
            posManager.cardReader.endWatch()
            posManager.cleanUpEmv()
        }
        super.onDestroy()
    }

    fun readCard() {
        stopTimer()
        posManager.sessionData.amount = viewModel.longAmount.value!!

        val cardLimit= 50000.0
        if (cardLimit < posManager.sessionData.amount / 100) {
            showError("The limit for this transaction is NGN${cardLimit}")
            return
        }

        mainScope.launch {
            posManager.cardReader.endWatch()
            val cardData = posManager.cardReader.read(viewModel.amountCurrencyFormat.value!!)
            if (cardData == null) {
                renderTransactionFailure("Transaction Cancelled", "")
                return@launch
            }
            this@CardTransactionActivity.cardData = cardData

            when (CardTransactionStatus.find(cardData.ret)) {
                CardTransactionStatus.Success -> {
                    if (cardData.pan.isEmpty()) {
                        dialogProvider.hideProgressBar()
                        renderTransactionFailure("Could not read card")

                        return@launch
                    }

                    val thisMonth = Instant.now().format("yyMM").toInt()

                    if (cardData.exp.substring(0, 4).toInt() < thisMonth) {
                        dialogProvider.hideProgressBar()
                        renderTransactionFailure("Invalid Card")

                        return@launch
                    }

                    val route = getSupportedRoute(cardData.pan, viewModel.amount.value!!)
                    if (route == InvalidRemoteConnectionInfo) {
                        dialogProvider.hideProgressBar()
                        renderTransactionFailure("No supported route for this card/amount combination")
                        return@launch
                    }

                    onReadCard(cardData)
                }
                CardTransactionStatus.UserCancel -> renderTransactionFailure("Transaction Cancelled")
                CardTransactionStatus.Failure -> renderTransactionFailure("Failed to read card")
                CardTransactionStatus.Timeout -> renderTransactionFailure("Timeout while reading card")
                CardTransactionStatus.OfflinePinVerifyError -> renderTransactionFailure("Wrong PIN. Card Restricted")
                CardTransactionStatus.CardRestricted -> renderTransactionFailure("Card Restricted")
                CardTransactionStatus.CardExpired -> renderTransactionFailure("Card Expired")
                CardTransactionStatus.NoPin -> renderTransactionFailure("No PIN entered")
                else -> renderTransactionFailure(EmvErrorMessage[cardData.ret])
            }
        }
    }

    private fun getPosParameter(pan: String, amount: Double): PosParameter {
        return getSupportedRoute(pan, amount).getParameter(this)
    }

    private fun getSupportedRoute(pan: String, amount: Double): RemoteConnectionInfo {
        val supportedRoute = posPreferences.binRoutes?.getSupportedRoute(pan, amount)
        return supportedRoute ?: config.remoteConnectionInfo
    }

    fun makeRequest(request: ISOMsg) = mainScope.launch {
        stopTimer()
        val supportedRoute =
            posPreferences.binRoutes?.getSupportedRoute(request.pan!!, viewModel.amount.value!!)
        val remoteConnectionInfo = supportedRoute ?: config.remoteConnectionInfo
        posParameter = remoteConnectionInfo.getParameter(this@CardTransactionActivity)

        request.apply {
            applyManagementData(posParameter.managementData)
            acquiringInstIdCode32 = localStorage.institutionCode
            terminalId41 = config.terminalId
        }

        val cardType = when (cardData.aid) {
            "A0000000032020" -> "VISA"
            "A0000000031010", "A0000000032010" -> "VISA Debit"
            "A0000004540010" -> "Etranzact Genesis Card"
            "A0000004540011" -> "Etranzact Genesis Card"
            "A0000000042203" -> "MasterCard US"
            "A0000000041010" -> "MasterCard"
            "A0000000044010" -> "MasterCard"
            "A0000000041030" -> "MasterCard"
            "A0000000046000" -> "MasterCard Specific"
            "A0000000043060" -> "MasterCard Specific"
            "A0000000042010" -> "MasterCard Specific"
            "A0000000043010" -> "MasterCard Specific"
            "A0000000045010" -> "MasterCard Specific"
            "A0000003710001" -> "InterSwitch Verve Card"
            else -> "Unknown Card"
        }

        val isoSocketHelper = IsoSocketHelper(
            config = config,
            parameters = posParameter,
            remoteConnectionInfo = remoteConnectionInfo,
        )
        val amount = request.transactionAmount4?.toDoubleOrNull()?.div(100)
        val posTransaction = PosTransaction.create(
            isoMsg = request,
            institutionCode = "100616",
            appName = "${getString(R.string.app_name)} ${appConfig.versionName}",
            ptsp = getString(R.string.ptsp_name),
            website = getString(R.string.institution_website),
            bankName = getString(R.string.pos_acquirer),
            cardHolder = cardData.holder,
            cardType = cardType,
            nodeName = remoteConnectionInfo.nodeName,
            responseCode = "XX",
            amountString = amount?.toCurrencyFormat() ?: "NGN0.00",
        )
        if (cardData.pinBlock.isEmpty()) {
            dialogProvider.showProgressBar("Pin Ok")
            delay(1000)
        }

        dialogProvider.showProgressBar("Receiving...")
        callHomeService.stopCallHomeTimer()

        val requeryConfig = remoteConnectionInfo.requeryConfig
        val (response, error) = withContext(Dispatchers.IO) {
            if (request.mti != "0200" || requeryConfig == null || requeryConfig.maxRetries < 1) {
                return@withContext isoSocketHelper.send(request)
            }

            handleRetryableRequest(
                isoSocketHelper = isoSocketHelper,
                requeryConfig = requeryConfig,
                request = request,
            )
        }

        try {
            callHomeService.startCallHomeTimer()

            if (response == null) {
                showTransactionStatusPage(posTransaction)

                val receipt = posReceipt(
                    posTransaction = posTransaction,
                    isCustomerCopy = true,
                )
                printer.print(receipt)
                return@launch
            }

            // Abort if response rrn does not match request
            if (request.retrievalReferenceNumber37 != response.retrievalReferenceNumber37) {
                response.responseCode39 = "X6"
                posTransaction.responseCode = "X6"
            }

            response.set(4, request.transactionAmount4)


            if (response.isSuccessful) {
                if (transactionType == TransactionType.Balance) {
                    val additionalAmount = response.additionalAmounts54?.substring(8,20)
                    val amountDouble = additionalAmount?.toDoubleOrNull()?.div(100)
                    posTransaction.amount = amountDouble?.toCurrencyFormat() ?: "NGN0.00"
                }
            }

            showTransactionStatusPage(posTransaction)
            val receipt = posReceipt(
                posTransaction = posTransaction,
                isCustomerCopy = true
            )
            printer.print(receipt)
        } catch (ex: Exception) {
            showTransactionStatusPage(posTransaction)
            firebaseCrashlytics.recordException(ex)
            debugOnly { Log.e("CardTrans", ex.message, ex) }
        } finally {
            dialogProvider.hideProgressBar()
        }
    }

    private suspend fun handleRetryableRequest(
        requeryConfig: RequeryConfig,
        request: ISOMsg,
        isoSocketHelper: IsoSocketHelper,
    ): SafeRunResult<ISOMsg> {
        var result = SafeRunResult<ISOMsg>(null)

        for (attempt in 1..(requeryConfig.maxRetries + 1)) {
            val isRetry = attempt > 1
            if (isRetry) {
                delay(20000)
                request.mti =
                    if (attempt > 2 && result.error !is ConnectException) "0221" else "0220"
                runOnUiThread { dialogProvider.showProgressBar("Retrying...$attempt") }
            }

            val newResult = isoSocketHelper.send(request, isRetry)

            // Criteria for adopting new result
            if (
                result.data == null
                || result.data!!.responseCode39 != "00" // previous response code failed
                || request.retrievalReferenceNumber37 != result.data!!.retrievalReferenceNumber37 // previous response rrn mismatch
            ) {
                result = newResult
            }

            // Continue on error
            val responseMsg = result.data ?: continue

            // Continue on rrn mismatch
            if (request.retrievalReferenceNumber37 != responseMsg.retrievalReferenceNumber37) {
                continue
            }

            // Continue on "Issuer or switch" response
            if (responseMsg.responseCode39 == "91") {
                continue
            }

            // Exit loop on success
            if (result.error == null) {
                break
            }
        }

        return result
    }

    fun processingCode(code: String) = "$code${viewModel.accountType.value?.code ?: "00"}00"

    fun goBack(view: View?) {
        onBackPressed()
    }

    protected fun showAmountPage() =
        showAmountPage("Enter Amount (Naira)") { amount ->
            viewModel.amountString.value = amount.toString()
            sessionData.amount = amount
            sessionData.transactionType = transactionType
            confirmAmounts(sessionData) {
                readCard()
            }
        }

    abstract fun onSelectAccountType()
    abstract fun onReadCard(cardData: CardData)

    internal fun onTransactionDidFinish() {
        posManager.cardReader.endWatch()
        val cardReaderEvent = viewModel.cardReaderEvent.value
        if (cardReaderEvent == CardReaderEvent.CHIP || cardReaderEvent == CardReaderEvent.CHIP_FAILURE) {
            mainScope.launch {
                posManager.cardReader.onRemoveCard {
                    if (it == CardReaderEvent.REMOVED || it == CardReaderEvent.CANCELLED) {
                        finish()
                    }
                }
            }
        }
    }

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                posManager.cardReader.endWatch()
                finish()
            }
        }
    }
}
