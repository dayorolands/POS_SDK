package com.urovo.v67

import android.device.IccManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardReaderEventListener
import com.cluster.pos.card.CardReaders
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.sdk.insertcard.InsertCardHandlerImpl
import com.urovo.sdk.insertcard.utils.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Hashtable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class UrovoCardReader(
    private val activity: CreditClubActivity,
    private val sessionData: PosManager.SessionData,
    private val emvNfcKernelApi: EmvNfcKernelApi
) : CardReaders, KoinComponent {
    private val dialogProvider = activity.dialogProvider
    private val mICReader = IccManager()
    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED
    private var checkCardMode = ContantPara.CheckCardMode.SWIPE_OR_INSERT_OR_TAP

    private val cardReader = InsertCardHandlerImpl.getInstance()
    private val cardType = Constant.Mode.MODE_USER
    private var supportsChip = true

    private var userCancel = false
    private var isSessionOver = false


    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar(
            title = "Please insert card",
            message = "Waiting...",
            isCancellable = true
        ) {
            onClose {
                userCancel = true
                deviceClose()
                if (!activity.isFinishing) activity.finish()
            }
        }

        delay(200)
        cardReaderEvent = suspendCoroutine { continuation ->
            detectCard(continuation)
        }
        dialogProvider.hideProgressBar()
        return cardReaderEvent
    }

    override suspend fun read(amountStr: String): CardData? {
        val cardData : CardData? = suspendCoroutine { continuation ->
            if(!cardReader.isCardIn){
                return@suspendCoroutine dialogProvider.showError("No card detected, please insert card"){
                    onClose {
                        CoroutineScope(Dispatchers.Main).launch {
                            waitForCard()
                        }
                    }
                }
            }
            dialogProvider.showProgressBar("Processing", "IC Card Detected...")
            val emvListener = UrovoListener(activity, emvNfcKernelApi, sessionData, continuation)

            CoroutineScope(Dispatchers.IO).launch {
                startKernel(checkCardMode)
            }
            emvNfcKernelApi.setListener(emvListener)
            emvNfcKernelApi.setContext(activity)
        }
        deviceClose()
        dialogProvider.hideProgressBar()
        return cardData
    }
    private fun startKernel(checkCardMode : ContantPara.CheckCardMode){
        val emvTransactionData = Hashtable<String, Any>()
        val divideAmount = sessionData.amount / 100
        emvTransactionData.apply {
            put("checkCardMode", checkCardMode)
            put("currencyCode", "566")
            put("emvOption", ContantPara.EmvOption.START)
            put("amount", divideAmount.toString())
            put("checkCardTimeout", "30")
            put("transactionType", "00")
            put("FallbackSwitch", "0")
            put("supportDRL", true)
            put("enableBeeper", false)
            put("enableTapSwipeCollision", false)
        }
        emvNfcKernelApi.updateTerminalParamters(ContantPara.CardSlot.UNKNOWN, "9F3303E0F0C89F1A020566")
        emvNfcKernelApi.startKernel(emvTransactionData)
    }

    override fun endWatch() {
        cardReader.powerDown(cardType)
        emvNfcKernelApi.abortKernel()
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        if (!userCancel && !isSessionOver) {
            userCancel = true
            deviceClose()
            onEventChange(CardReaderEvent.REMOVED)
        }
    }

    private fun detectCard(continuation: Continuation<CardReaderEvent>) {
        val atrData = ByteArray(64)
        while (true) {
            if (userCancel) {
                return continuation.resume(CardReaderEvent.CANCELLED)
            }
            if(supportsChip){
                val ret = mICReader.open(cardType, "1".toByte(), 1)
                if (ret == 0) {
                    val powerOn = cardReader.powerUp(cardType, atrData)
                    if(powerOn > 0) return continuation.resume(CardReaderEvent.CHIP)
                }
                else if (ret < 0) {
                    updateCardWaitingProgress("ICC Failure. Please Swipe Card")
                    return continuation.resume(CardReaderEvent.CHIP_FAILURE)
                }
            }
        }
    }

    private fun updateCardWaitingProgress(text: String = "Please insert card") {
        dialogProvider.showProgressBar(text, "Waiting...", isCancellable = true) {
            onClose {
                userCancel = true
                deviceClose()
                if (!activity.isFinishing) activity.finish()
            }
        }
    }

    private fun deviceClose(){
        cardReader.powerDown(cardType)
        emvNfcKernelApi.abortKernel()
    }
}