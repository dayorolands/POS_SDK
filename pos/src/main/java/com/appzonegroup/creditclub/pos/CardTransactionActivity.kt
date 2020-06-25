package com.appzonegroup.creditclub.pos

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.appzonegroup.creditclub.pos.card.*
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.data.create
import com.appzonegroup.creditclub.pos.databinding.PageInputRrnBinding
import com.appzonegroup.creditclub.pos.databinding.PageSelectAccountTypeBinding
import com.appzonegroup.creditclub.pos.databinding.PageTransactionErrorBinding
import com.appzonegroup.creditclub.pos.databinding.PageVerifyCashoutBinding
import com.appzonegroup.creditclub.pos.extension.format
import com.appzonegroup.creditclub.pos.models.*
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.models.messaging.ReversalRequest
import com.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.creditclub.pos.printer.Receipt
import com.appzonegroup.creditclub.pos.service.ApiService
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.util.indicateError
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.showErrorAndWait
import com.creditclub.pos.PosManager
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.card.CardTransactionStatus
import com.google.gson.Gson
import kotlinx.android.synthetic.main.page_input_amount.*
import kotlinx.android.synthetic.main.page_input_rrn.*
import kotlinx.android.synthetic.main.text_field.view.*
import kotlinx.coroutines.*
import okhttp3.Headers
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.threeten.bp.Instant
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("Registered")
abstract class CardTransactionActivity : PosActivity(), View.OnClickListener {
    private val posManager: PosManager by inject { parametersOf(this) }

    private var amountText = "0"
    protected var previousMessage: CardIsoMsg? = null
    private var pendingRequest: CardIsoMsg? = null
    private var accountType: AccountType? = null

    private lateinit var cardData: CardData
    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED

    internal val canPerformTransaction: Boolean
        get() {
            return parameters.run {
                pinKey.isNotEmpty() && masterKey.isNotEmpty() && sessionKey.isNotEmpty() && pfmd.isNotEmpty()
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (parameters.pinKey.isEmpty() || parameters.masterKey.isEmpty() || parameters.sessionKey.isEmpty()) {
            return showError("Please perform key download before proceeding") {
                onClose {
                    finish()
                }
            }
        }

        if (parameters.pfmd.isEmpty()) {
            return showError("Please perform parameter download before proceeding") {
                onClose {
                    finish()
                }
            }
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }

        registerReceiver(mBatInfoReceiver, filter)
    }

    fun requestCard() {
        if (!canPerformTransaction) return

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

                else -> {
                    cardReaderEvent = cardEvent
                    restartTimer()
//                            accountType = AccountType.Default
//                            onSelectAccountType()
                    val binding = DataBindingUtil.setContentView<PageSelectAccountTypeBinding>(
                        this@CardTransactionActivity,
                        R.layout.page_select_account_type
                    )

                    listOf(
                        binding.creditRadioButton,
                        binding.currentRadioButton,
                        binding.defaultRadioButton,
                        binding.savingsRadioButton
                    ).forEach { it.root.setOnClickListener(this@CardTransactionActivity) }

                    if (cardEvent == CardReaderEvent.CHIP) {
                        mainScope.launch {
                            posManager.cardReader.onRemoveCard { finish() }
                        }
                    }
                }
            }
        }
    }

    private var isTimerRunning = false
    private var timeoutTimer: TimerTask? = null

    private fun createTimer(): TimerTask {
        return Timer().schedule(60000) {
            runOnUiThread {
                renderTransactionFailure("Timeout due to card holder inactivity")
                stopTimer()
            }
        }
    }

    private fun restartTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            timeoutTimer = createTimer()
        } else {
            isTimerRunning = false
            timeoutTimer?.cancel()
            timeoutTimer = null
            restartTimer()
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            timeoutTimer?.cancel()
            timeoutTimer = null
        }
    }

    override fun onDestroy() {
        try {
            stopTimer()
            unregisterReceiver(mBatInfoReceiver)
            posManager.cardReader.endWatch()
            posManager.cleanUpEmv()
        } catch (e: Exception) {
//            analyticsHelper.logException(e)
            e.printStackTrace()
        } finally {
            super.onDestroy()
        }
    }

    fun next(view: View?) {
        when (view?.id) {
            R.id.select_amount_button -> {
                if (amountText.toLong() == 0L) return indicateError("Amount cannot be zero", null)
                if (amountText.isEmpty()) return indicateError("Please specify amount", null)

                readCard()
            }

            R.id.confirm_cashout_button -> {
                finish()
            }

            R.id.cancel_button -> {
                if (amountText.isNotEmpty() && amountText.toLong() > 0) finish()
                else renderTransactionFailure("Please Remove Card", "")
            }

            R.id.confirm_rrn_button -> {
                PosDatabase.open(this, Dispatchers.Main) { db ->
                    val trn = withContext(Dispatchers.Default) {
                        db.financialTransactionDao().byRRN(rrn_field.input.text.toString())
                    } ?: return@open showError("Retrieval Reference Number error")

                    amountText = trn.isoMsg.transactionAmount4?.toInt().toString()
                    previousMessage = trn.isoMsg
                    requestCard()
                }
            }
        }
    }

    fun readCard() {
        try {
            stopTimer()
            posManager.sessionData.amount = amountText.toLong()

            val cardLimit: Double = localStorage.agent?.cardLimit ?: 50000.0
            if (cardLimit < posManager.sessionData.amount / 100) {
                showError("The limit for this transaction is NGN${cardLimit}")
                return
            }

            val amountStr = format(amountText)

            mainScope.launch {
                posManager.cardReader.endWatch()
                val cardData = posManager.cardReader.read(amountStr)
                cardData ?: return@launch renderTransactionFailure("Transaction Cancelled", "")
                this@CardTransactionActivity.cardData = cardData

                when (CardTransactionStatus.find(cardData.ret)) {
                    CardTransactionStatus.Success -> {

                        if (cardData.pan.isEmpty()) {
                            dialogProvider.hideProgressBar()
                            renderTransactionFailure("Could not read card")

                            return@launch
                        }

                        val thisMonth = Instant.now().format("YYMM").toInt()

                        if (cardData.exp.substring(0, 4).toInt() < thisMonth) {
                            dialogProvider.hideProgressBar()
                            renderTransactionFailure("Invalid Card")

                            return@launch
                        }

                        cardData.pinBlock = posManager.sessionData.pinBlock ?: cardData.pinBlock
                        if (cardData.pinBlock.isNullOrBlank()) {
                            hideProgressBar()
                            renderTransactionFailure("Could not validate PIN")
                            return@launch
                        }

                        onReadCard(cardData)
                    }
                    CardTransactionStatus.UserCancel -> renderTransactionFailure("Transaction Cancelled")
                    CardTransactionStatus.Failure -> renderTransactionFailure("Wrong PIN. Card Restricted")
                    CardTransactionStatus.Timeout -> renderTransactionFailure("Timeout while reading card")
                    CardTransactionStatus.OfflinePinVerifyError -> renderTransactionFailure("Wrong PIN. Card Restricted")
                    else -> renderTransactionFailure(EmvErrorMessage[cardData.ret])
                }
            }
        } catch (ex: Exception) {
//            analyticsHelper.logException(ex)
            renderTransactionFailure(ex.message ?: "An internal error occurred")
        }
    }

    fun makeRequest(request: CardIsoMsg) {
        pendingRequest = request
        stopTimer()

        GlobalScope.launch(Dispatchers.Main) {
            if (cardData.pinBlock.isEmpty()) {
                dialogProvider.showProgressBar("Pin Ok")
                delay(1000)
            }
//            else {
//                request.apply {
//                    withPinData(cardData.pinBlock, parameters)
//                }
//            }

            dialogProvider.showProgressBar("Receiving...")
            try {
                callHomeService.stopCallHomeTimer()

                val (response) = withContext(Dispatchers.Default) {
                    isoSocketHelper.send(request)
                }

                callHomeService.startCallHomeTimer()

                if (response == null) {
//                    analyticsHelper.logException(error)
                    renderTransactionFailure("Transmission Error")
                    attemptReversal(request)

                    return@launch
                }

                response.dump(System.out, "response:")

                response.set(4, request.transactionAmount4)

                val transaction = FinancialTransaction(response).apply {
                    createdAt = Instant.now()
                    cardHolder = cardData.holder
                    aid = cardData.aid
                    cardType = cardData.type
                }

                val receipt = Receipt(this@CardTransactionActivity, transaction)


                onTransactionDidFinish()

                val binding = DataBindingUtil.setContentView<PageVerifyCashoutBinding>(
                    this@CardTransactionActivity,
                    R.layout.page_verify_cashout
                )

                if (response.isSuccessful) {
                    binding.message.text = getString(R.string.transaction_successful)
                    binding.transactionStatusIcon.setImageResource(R.drawable.ic_sentiment_satisfied)
                    ImageViewCompat.setImageTintList(
                        binding.transactionStatusIcon, ColorStateList.valueOf(
                            ContextCompat.getColor(this@CardTransactionActivity, R.color.posPrimary)
                        )
                    )
                } else {
                    binding.message.text = response.responseMessage
                    binding.transactionStatusIcon.setImageResource(R.drawable.ic_sentiment_very_dissatisfied)
                    ImageViewCompat.setImageTintList(
                        binding.transactionStatusIcon, ColorStateList.valueOf(
                            ContextCompat.getColor(this@CardTransactionActivity, R.color.app_orange)
                        )
                    )
                }

                if (cardTransactionType(response) == TransactionType.Balance) {
                    val balance = try {
                        response.getString(54)?.substring(8, 20) ?: "0"
                    } catch (ex: java.lang.Exception) {
                        "0"
                    }

                    binding.amountText.text = CurrencyFormatter.format(balance)
                    binding.printMerchantCopy.visibility = View.GONE
                } else {
                    binding.amountText.text = format(amountText)

                    withContext(Dispatchers.IO) {
                        val db = PosDatabase.getInstance(this@CardTransactionActivity)

                        db.runInTransaction {
                            db.financialTransactionDao().save(transaction)
                            db.posTransactionDao().save(PosTransaction.create(response).apply {
                                bankName = getString(R.string.pos_acquirer)
                                cardHolder = cardData.holder
                                cardType = cardData.type
                            })
                        }

                        if (response.isSuccessful) {
                            val posNotification = PosNotification.create(transaction)
                            db.posNotificationDao().save(posNotification)

                            val url =
                                "${backendConfig.apiHost}/CreditClubMiddlewareAPI/CreditClubStatic/POSCashOutNotification"

                            val dataToSend = Gson().toJson(posNotification)

                            val headers = Headers.Builder()
                            headers.add(
                                "Authorization",
                                "iRestrict ${backendConfig.posNotificationToken}"
                            )
                            headers.add("TerminalID", config.terminalId)

                            val (responseString, error) = ApiService.post(
                                url,
                                dataToSend,
                                headers.build()
                            )

                            responseString ?: return@withContext
                            error?.printStackTrace()

                            try {
                                val notificationResponse =
                                    Gson().fromJson(
                                        responseString,
                                        NotificationResponse::class.java
                                    )
                                if (notificationResponse != null) {
                                    if (notificationResponse.billerReference != null && notificationResponse.billerReference!!.isNotEmpty()) {
                                        db.posNotificationDao().delete(posNotification.id)
                                    }
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    }
                }

                binding.printMerchantCopy.setOnClickListener {
                    printer.printAsync(receipt.apply {
                        isCustomerCopy = false
                    })
                }

                binding.printCustomerCopy.setOnClickListener {
                    printer.printAsync(receipt.apply {
                        isCustomerCopy = true
                    })
                }

                printer.printAsync(receipt.apply {
                    isCustomerCopy = true
                })
            } catch (ex: Exception) {
//                analyticsHelper.logException(ex)
                ex.printStackTrace()
                renderTransactionFailure("Transmission Error")

                attemptReversal(request)
            } finally {
                dialogProvider.hideProgressBar()
            }
        }
    }

    private suspend fun attemptReversal(request: BaseIsoMsg) {
        if (request.mti == "0200") withContext(Dispatchers.Default) {
            runOnUiThread {
                dialogProvider.showProgressBar("Transmission Error \nReversing...")
            }

            delay(1000)

            val reversal = ReversalRequest.generate(request, cardData).apply {
                processingCode3 = processingCode("00")
                messageReasonCode56 = "4021"
                withParameters(parameters.parameters)
            }

            val success = isoSocketHelper.attempt(reversal, 4, onReattempt = {
                runOnUiThread {
                    dialogProvider.showProgressBar("Reversing...$it")
                }

                delay(1000)

                reversal.mti = "421"
            })

//            if (success) analyticsHelper.logTransaction(TransactionType.Reversal.type, request, "Manual")

            if (!success) {
                PosDatabase
                    .getInstance(this@CardTransactionActivity)
                    .reversalDao()
                    .save(Reversal(reversal))
            }
        }
    }

    fun processingCode(code: String) = "$code${accountType?.code ?: "00"}00"

    override fun onClick(v: View?) = selectAccountType(v)

    fun onSelectNumber(view: View) {
        restartTimer()
        if (amountText.length > 7) return
        val num = when (view.id) {
            R.id.number1 -> 1
            R.id.number2 -> 2
            R.id.number3 -> 3
            R.id.number4 -> 4
            R.id.number5 -> 5
            R.id.number6 -> 6
            R.id.number7 -> 7
            R.id.number8 -> 8
            R.id.number9 -> 9
            else -> 0
        }

        amountText = "$amountText$num".toLong().toString()
        amountTv.text = format(amountText).replace("NGN", "")
    }

    fun onSelectPresetNumber(view: View?) {
        restartTimer()
        amountText = when (view?.id) {
            R.id.number2000 -> "200000"
            R.id.number5000 -> "500000"
            R.id.number10000 -> "1000000"
            else -> ""
        }

        amountTv.text = format(amountText).replace("NGN", "")
    }

    fun onBackspacePressed(view: View?) {
        restartTimer()
//        if (amountText.isEmpty()) return
//        amountText = amountText.substring(0, amountText.length - 1)
//        if (amountText.isEmpty())
        amountText = "0"
        amountTv.text = format(amountText).replace("NGN", "")
    }

    fun format(text: String): String = CurrencyFormatter.format(text)

    fun goBack(view: View?) {
        onBackPressed()
    }

    private fun selectAccountType(view: View?) {
        accountType = when (view?.id) {
            R.id.current_radio_button -> AccountType.Current
            R.id.savings_radio_button -> AccountType.Savings
            R.id.credit_radio_button -> AccountType.Credit
            R.id.default_radio_button -> AccountType.Default
            else -> null
        }

        onSelectAccountType()
    }

    fun showAmountPage() {
        restartTimer()
        DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.page_input_amount)

        title = "Amount"
        amountText = "0"
        amountTv.text = format(amountText).replace("NGN", "")
    }


    fun showReferencePage(pageTitle: String = "Enter RRN") {
        if (!canPerformTransaction) return

        mainScope.launch {
            if (Platform.hasPrinter) {
                val printerStatus = withContext(Dispatchers.Default) { printer.check() }
                if (printerStatus != PrinterStatus.READY) {
                    dialogProvider.showErrorAndWait(printerStatus.message)
                    finish()
                    return@launch
                }
            }
            val binding = DataBindingUtil.setContentView<PageInputRrnBinding>(
                this@CardTransactionActivity,
                R.layout.page_input_rrn
            )

            title = pageTitle
            binding.title = pageTitle
            amountText = "0"
        }
    }

    abstract fun onSelectAccountType()
    abstract fun onReadCard(cardData: CardData)

    private fun renderTransactionFailure(
        message: String = "",
        subMessage: String = "Please remove card"
    ) {

        val binding = DataBindingUtil.setContentView<PageTransactionErrorBinding>(
            this,
            R.layout.page_transaction_error
        )
        binding.message.text = message
        binding.subMessage.text = if (cardReaderEvent == CardReaderEvent.CHIP) subMessage else ""

        onTransactionDidFinish()

        binding.closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun onTransactionDidFinish() {
        posManager.cardReader.endWatch()

        if (cardReaderEvent == CardReaderEvent.CHIP || cardReaderEvent == CardReaderEvent.CHIP_FAILURE) {
            GlobalScope.launch {
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
            val action = intent.action
            if (Intent.ACTION_SCREEN_ON == action) {
            } else if (Intent.ACTION_SCREEN_OFF == action) {
                posManager.cardReader.endWatch()
                finish()
            }
        }
    }
}

