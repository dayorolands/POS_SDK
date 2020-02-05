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
import com.appzonegroup.creditclub.pos.command.StartEmvService
import com.appzonegroup.creditclub.pos.command.StartPinPadService
import com.appzonegroup.creditclub.pos.contract.Logger
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.data.create
import com.appzonegroup.creditclub.pos.databinding.PageInputRrnBinding
import com.appzonegroup.creditclub.pos.databinding.PageTransactionErrorBinding
import com.appzonegroup.creditclub.pos.databinding.PageVerifyCashoutBinding
import com.appzonegroup.creditclub.pos.extension.format
import com.appzonegroup.creditclub.pos.models.*
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.models.messaging.ReversalRequest
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.creditclub.pos.printer.Receipt
import com.appzonegroup.creditclub.pos.service.ApiService
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.appzonegroup.creditclub.pos.util.StableAPPCAPK
import com.creditclub.core.util.localStorage
import com.google.gson.Gson
import com.telpo.emv.EmvService
import com.telpo.pinpad.PinpadService
import kotlinx.android.synthetic.main.page_input_amount.*
import kotlinx.android.synthetic.main.page_input_rrn.*
import kotlinx.android.synthetic.main.text_field.view.*
import kotlinx.coroutines.*
import okhttp3.Headers
import org.threeten.bp.Instant
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("Registered")
abstract class CardTransactionActivity : PosActivity(), Logger, View.OnClickListener {
    private val cardReader by lazy { CardReader(this, emvListener) }
    private val emvService by lazy { EmvService.getInstance() }
    private val emvListener by lazy { CustomEmvServiceListener(this, emvService) }

    override val tag: String = "CardTrans"

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

    private suspend fun loadEmv() = withContext(Dispatchers.Default) {
        try {
            PinpadService.Close()
        } catch (ex: Exception) {
//            analyticsHelper.logException(ex)
            ex.printStackTrace()
        }

        emvService.setListener(emvListener)

        EmvService.Emv_SetDebugOn(if (BuildConfig.DEBUG) 1 else 0)

        StartEmvService(this@CardTransactionActivity).run()
        StartPinPadService(this@CardTransactionActivity, parameters).run()

        EmvService.Emv_RemoveAllApp()
        EmvService.Emv_RemoveAllCapk()

        StableAPPCAPK.Add_All_APP()
        StableAPPCAPK.Add_All_CAPK()
    }

    fun requestCard() {
        if (!canPerformTransaction) return

        mainScope.launch {
            dialogProvider.showProgressBar("Loading card functions")
            loadEmv()
            delay(1000)
            dialogProvider.hideProgressBar()

            printerDependentAction(true) {
                cardReader.waitForCard { cardEvent ->
                    when (cardEvent) {
                        CardReaderEvent.REMOVED, CardReaderEvent.CANCELLED -> {
                            stopTimer()
                            finish()
                            return@waitForCard
                        }

                        CardReaderEvent.CHIP_FAILURE -> {
                            stopTimer()
                            renderTransactionFailure("Please Remove Card", "")
                            return@waitForCard
                        }

                        else -> {
                            cardReaderEvent = cardEvent
                            restartTimer()
                            accountType = AccountType.Default
                            onSelectAccountType()
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
            cardReader.endWatch()
            PinpadService.Close()
            EmvService.deviceClose()

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
            emvListener.amount = amountText.toLong()

            val cardLimit: Double = localStorage.agent?.cardLimit ?: 50000.0
            if (cardLimit < emvListener.amount / 100) {
                showError("The limit for this transaction is NGN${cardLimit}")
                return
            }

            val amountStr = format(amountText)

            cardReader.endWatch()
            cardReader.read(amountStr) { cardData ->
                cardData ?: return@read renderTransactionFailure("Transaction Cancelled", "")
                this@CardTransactionActivity.cardData = cardData

                when (cardData.ret) {
                    EmvService.EMV_TRUE -> {

                        if (cardData.pan.isEmpty()) {
                            hideProgressBar()
                            renderTransactionFailure("Could not read card")

                            return@read
                        }

                        val thisMonth = Instant.now().format("YYMM").toInt()

                        if (cardData.exp.substring(0, 4).toInt() < thisMonth) {
                            hideProgressBar()
                            renderTransactionFailure("Invalid Card")

                            return@read
                        }

                        cardData.pinBlock = emvListener.pinBlock ?: cardData.pinBlock
                        onReadCard(cardData)
                    }
                    EmvService.ERR_USERCANCEL -> renderTransactionFailure("Transaction Cancelled")
                    EmvService.EMV_FALSE -> renderTransactionFailure("Wrong PIN. Card Restricted")
                    EmvService.ERR_TIMEOUT -> renderTransactionFailure("Timeout while reading card")
                    EmvService.ERR_OFFLINE_PIN_VERIFY_ERROR -> renderTransactionFailure("Wrong PIN. Card Restricted")
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
                showProgressBar("Pin Ok")
                delay(1000)
            }
//            else {
//                request.apply {
//                    withPinData(cardData.pinBlock, parameters)
//                }
//            }

            showProgressBar("Receiving...")
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
                                cardHolder = cardData.holder
                                cardType = cardData.type
                            })
                        }

                        if (response.isSuccessful) {
                            val posNotification = PosNotification.create(transaction)
                            db.posNotificationDao().save(posNotification)

                            val url = "${ApiService.BASE_URL}/POSCashOutNotification"

                            val dataToSend = Gson().toJson(posNotification)
                            log("PosNotification request: $dataToSend")

                            val headers = Headers.Builder()
                            headers.add(
                                "Authorization",
                                "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}"
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
                hideProgressBar()
            }
        }
    }

    private suspend fun attemptReversal(request: BaseIsoMsg) {
        if (request.mti == "0200") withContext(Dispatchers.Default) {
            runOnUiThread {
                showProgressBar("Transmission Error \nReversing...")
            }

            delay(1000)

            val reversal = ReversalRequest.generate(request, cardData).apply {
                processingCode3 = processingCode("00")
                messageReasonCode56 = "4021"
                withParameters(parameters.parameters)
            }

            val success = isoSocketHelper.attempt(reversal, 4, onReattempt = {
                runOnUiThread {
                    showProgressBar("Reversing...$it")
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

        printer.checkAsync { printerStatus ->
            if (printerStatus != PrinterStatus.READY) {
                showError(printerStatus.message) {
                    onClose {
                        finish()
                    }
                }

                return@checkAsync
            }

            val binding =
                DataBindingUtil.setContentView<PageInputRrnBinding>(this, R.layout.page_input_rrn)

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
        cardReader.endWatch()

        if (cardReaderEvent == CardReaderEvent.CHIP || cardReaderEvent == CardReaderEvent.CHIP_FAILURE) {
            GlobalScope.launch {
                cardReader.startWatch {
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
                log("-----------------screen is on...")
            } else if (Intent.ACTION_SCREEN_OFF == action) {
                cardReader.endWatch()
                finish()
                log("----------------- screen is off...")
                //wakeLock.acquire();
                log("acquire ?")
                //wakeScreen(EmvActivity.this);
                log("wakeScreen ?")

                //wakeUpAndUnlock(context);
                log("wakeUpAndUnlock ?")
            }
        }
    }
}

