package com.cluster.pos

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
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
import com.cluster.pos.models.messaging.ReversalRequest
import com.cluster.pos.printer.PrinterStatus
import com.cluster.pos.printer.Receipt
import com.cluster.pos.service.CallHomeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jpos.iso.ISOMsg
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
    internal var previousMessage: CardIsoMsg? = null
    abstract var transactionType: TransactionType
    private lateinit var cardData: CardData
    private val posApiService: PosApiService by retrofitService()
    private val posPreferences: PosPreferences by inject()
    private val callHomeService: CallHomeService by inject()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }

        registerReceiver(mBatInfoReceiver, filter)
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
        val noKeysPresent = parameters.pinKey.isEmpty()
                || parameters.masterKey.isEmpty()
                || parameters.sessionKey.isEmpty()

        when {
            noKeysPresent -> {
                finishWithError("Please perform key download before proceeding")
                return
            }
            parameters.managementDataString.isEmpty() -> {
                finishWithError("Please perform parameter download before proceeding")
                return
            }
            else -> {
                sessionData.getDukptConfig = { pan, amount ->
                    posPreferences.binRoutes?.getSupportedRoute(pan, amount)?.dukptConfig
                }
                sessionData.getPosParameter = ::getPosParameter
                onPosReady()
            }
        }
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
//                            accountType = AccountType.Default
//                            onSelectAccountType()

                    showSelectAccountScreen { accountType ->
                        viewModel.accountType.value = accountType
                        onSelectAccountType()
                    }

                    if (cardEvent == CardReaderEvent.CHIP) {
                        mainScope.launch {
                            posManager.cardReader.onRemoveCard { finish() }
                        }
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
            unregisterReceiver(mBatInfoReceiver)
            posManager.cardReader.endWatch()
            posManager.cleanUpEmv()
        }
        super.onDestroy()
    }

    fun readCard() {
        stopTimer()
        posManager.sessionData.amount = viewModel.longAmount.value!!

        val cardLimit: Double = localStorage.agent?.cardLimit ?: 50000.0
        if (cardLimit < posManager.sessionData.amount / 100) {
            showError("The limit for this transaction is NGN${cardLimit}")
            return
        }

        mainScope.launch {
            posManager.cardReader.endWatch()
            val cardData = posManager.cardReader.read(viewModel.amountCurrencyFormat.value!!)
            cardData ?: return@launch renderTransactionFailure("Transaction Cancelled", "")
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

                    if (cardData.pinBlock.isBlank()) {
                        dialogProvider.hideProgressBar()
                        renderTransactionFailure("Could not validate PIN")
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

    fun makeRequest(request: ISOMsg) {
        stopTimer()
        val supportedRoute =
            posPreferences.binRoutes?.getSupportedRoute(request.pan!!, viewModel.amount.value!!)
        val remoteConnectionInfo = supportedRoute ?: config.remoteConnectionInfo
        val posParameter = remoteConnectionInfo.getParameter(this)
        request.applyManagementData(posParameter.managementData)
        request.acquiringInstIdCode32 = localStorage.institutionCode
        val isoSocketHelper = IsoSocketHelper(
            config = config,
            parameters = posParameter,
            remoteConnectionInfo = remoteConnectionInfo,
        )
        val posTransaction = PosTransaction.create(request).apply {
            bankName = getString(R.string.pos_acquirer)
            cardHolder = cardData.holder
            cardType = cardData.type
            nodeName = remoteConnectionInfo.nodeName
            responseCode = "XX"
        }
        mainScope.launch {
            if (cardData.pinBlock.isEmpty()) {
                dialogProvider.showProgressBar("Pin Ok")
                delay(1000)
            }

            dialogProvider.showProgressBar("Receiving...")
            try {
                callHomeService.stopCallHomeTimer()
                // Log transaction before making request
                val posTransactionId = posDatabase.posTransactionDao().save(posTransaction)
                posTransaction.apply { id = posTransactionId }

                val (response, error) = withContext(Dispatchers.IO) {
                    val maxAttempts = 1 + (remoteConnectionInfo.requeryConfig?.maxRetries ?: 0)
                    if (request.mti == "0200" && maxAttempts > 1) {
                        var result = SafeRunResult<ISOMsg>(null)
                        for (attempt in 1..maxAttempts) {
                            val isRetry = attempt > 1
                            if (isRetry) {
                                delay(2000)
                                request.mti =
                                    if (attempt > 2 && result.error !is ConnectException) "0221" else "0220"
                                runOnUiThread { dialogProvider.showProgressBar("Retrying...$attempt") }
                            }

                            val newResult = isoSocketHelper.send(request, isRetry)
                            if (result.data == null || (result.data!!.responseCode39 != "00" && newResult.isSuccess)) {
                                result = newResult
                            }
                            val responseMsg = result.data ?: continue
                            if (responseMsg.responseCode39 == "91") continue
                            if (result.error == null) break
                        }
                        result
                    } else {
                        isoSocketHelper.send(request)
                    }
                }
                callHomeService.startCallHomeTimer()

                if (error != null) {
                    firebaseCrashlytics.recordException(error)
                }
                if (response == null) {
                    renderTransactionFailure("Transmission Error")
                    isoSocketHelper.attemptReversal(request)

                    return@launch
                }

                response.set(4, request.transactionAmount4)

                val transaction = FinancialTransaction(response).apply {
                    createdAt = Instant.now()
                    cardHolder = cardData.holder
                    aid = cardData.aid
                    cardType = cardData.type
                    nodeName = remoteConnectionInfo.nodeName
                    if (remoteConnectionInfo is ConnectionInfo) {
                        connectionInfo = remoteConnectionInfo
                    }
                }

                withContext(Dispatchers.IO) {
                    posDatabase.runInTransaction {
                        posTransaction.apply {
                            responseCode = response.responseCode39
                        }
                        posDatabase.financialTransactionDao().save(transaction)
                        posDatabase.posTransactionDao().update(posTransaction)
                    }
                }

                if (response.isSuccessful) {
                    when (transactionType) {
                        TransactionType.Purchase,
                        TransactionType.CashAdvance,
                        TransactionType.CashBack,
                        TransactionType.PreAuth,
                        TransactionType.SalesComplete,
                        -> {
                            val posNotification = PosNotification.create(transaction)
                            posNotification.terminalId = config.terminalId
                            posNotification.nodeName = remoteConnectionInfo.nodeName
                            if (remoteConnectionInfo is ConnectionInfo) {
                                posNotification.connectionInfo = remoteConnectionInfo
                            }
                            withContext(Dispatchers.IO) {
                                posDatabase.posNotificationDao().save(posNotification)
                            }
                            posApiService.logPosNotification(
                                posDatabase,
                                appConfig,
                                posConfig,
                                posNotification
                            )
                        }
                        else -> {

                        }
                    }
                }

                showTransactionStatusPage(response, transaction)

                val receipt = Receipt(this@CardTransactionActivity, transaction).apply {
                    isCustomerCopy = true
                }
                printer.printAsync(receipt.apply {
                    isCustomerCopy = true
                })
            } catch (ex: Exception) {
                firebaseCrashlytics.recordException(ex)
                debugOnly { Log.e("CardTrans", ex.message, ex) }
                renderTransactionFailure("Transmission Error")

                isoSocketHelper.attemptReversal(request)
            } finally {
                dialogProvider.hideProgressBar()
            }
        }
    }

    private suspend fun IsoSocketHelper.attemptReversal(request: ISOMsg) {
        if (request.mti == "0200") withContext(Dispatchers.IO) {
            runOnUiThread {
                dialogProvider.showProgressBar("Transmission Error \nReversing...")
            }

            delay(1000)

            val reversal = ReversalRequest.generate(request, cardData).apply {
                processingCode3 = processingCode("00")
                messageReasonCode56 = "4021"
                withParameters(parameters.parameters)
            }

            val success = attempt(reversal, 4, onReattempt = {
                runOnUiThread {
                    dialogProvider.showProgressBar("Reversing...$it")
                }

                delay(1000)

                reversal.mti = "0421"
            })

            if (!success) {
                val reversalRecord = Reversal(reversal).apply {
                    nodeName = remoteConnectionInfo.nodeName
                    if (remoteConnectionInfo is ConnectionInfo) {
                        connectionInfo = remoteConnectionInfo
                    }
                }
                posDatabase.reversalDao().save(reversalRecord)
            }
        }
    }

    fun processingCode(code: String) = "$code${viewModel.accountType.value?.code ?: "00"}00"

    fun goBack(view: View?) {
        onBackPressed()
    }

    protected fun showAmountPage(title: String = "Enter Amount (Naira)") =
        showAmountPage(title) { amount ->
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

    private val mBatInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                posManager.cardReader.endWatch()
                finish()
            }
        }
    }
}

inline val CardData.type: String
    get() = when (aid) {
        "A0000000032020" -> "VISA"
        // "A0000000031010" -> "VISA Debit Credit Classic"
        "A0000000031010" -> "VISA Debit"
        // "A0000000031010" -> "VISA Credit"
        // "A0000000032010" -> "VISA Electron"
        "A0000004540010" -> "Etranzact Genesis Card"
        "A0000004540011" -> "Etranzact Genesis Card"
        "A0000000042203" -> "MasterCard US"
        "A0000000041010" -> "MasterCard"
        "A0000000042010" -> "MasterCard Specific"
        "A0000000043010" -> "MasterCard Specific"
        "A0000000045010" -> "MasterCard Specific"
        "A0000003710001" -> "InterSwitch Verve Card"
        else -> "Unknown Card"
    }