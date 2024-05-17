package com.orda.horizonpay

import android.os.Bundle
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.card.*
import com.horizonpay.smartpossdk.aidl.IAidlDevice
import com.horizonpay.smartpossdk.aidl.emv.AidEntity
import com.horizonpay.smartpossdk.aidl.emv.AidlCheckCardListener
import com.horizonpay.smartpossdk.aidl.emv.CapkEntity
import com.horizonpay.smartpossdk.aidl.emv.EmvTermConfig
import com.horizonpay.smartpossdk.aidl.emv.EmvTransData
import com.horizonpay.smartpossdk.aidl.magcard.TrackData
import com.horizonpay.smartpossdk.data.EmvConstant
import com.orda.horizonpay.utils.AidsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HorizonCardReader(
    private val activity: CreditClubActivity,
    private val device : IAidlDevice,
    private val sessionData: PosManager.SessionData,
) : CardReaders, KoinComponent{
    private var cardReader = device.cardReader
    private var emv = device.emvL2
    private val dialogProvider = activity.dialogProvider
    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED
    private var userCancel = false
    private var isSessionOver = false

    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar("Opening device", "Please wait...", true) {
            onClose {
                userCancel = true
                deviceClose()
            }
        }
        updateCardWaitingProgress("Insert or Tap Card")
        cardReaderEvent = withContext(Dispatchers.IO){
            detectCard()
        }
        dialogProvider.hideProgressBar()
        return cardReaderEvent
    }

    private fun deviceClose(){
        cardReader.cancelSearchCard()
    }

    override suspend fun read(amountStr: String): CardData? {
        val cardData : CardData? = suspendCoroutine { continuation ->
            dialogProvider.showProgressBar("Processing", if(cardReaderEvent == CardReaderEvent.NFC) "Please leave card on device" else "IC card detected...") {
                onClose {
                    continuation.resume(null)
                }
            }
            val terminalConfig = EmvTermConfig()
            terminalConfig.capability = "E0F0C8"
            terminalConfig.countryCode = "0566"
            terminalConfig.transCurrCode = "0566"
            terminalConfig.extCapability = "E000F0A001"
            terminalConfig.termType = 0x22
            terminalConfig.transCurrExp = 2
            emv.termConfig = terminalConfig
            val emvTransactionData = EmvTransData()
            emvTransactionData.amount = sessionData.amount
            emvTransactionData.otherAmount = 0
            emvTransactionData.isForceOnline = true
            emvTransactionData.emvFlowType = EmvConstant.EmvTransFlow.FULL
            setEmvTransactionDataExt()
            println("CHECK-POINT this is where it stops.")
            val emvListener = HorizonListener(
                activity = activity,
                device = device,
                sessionData = sessionData,
                cardReaderEvent = cardReaderEvent,
                continuation = continuation
            )
            emv.startEmvProcess(emvTransactionData, emvListener)
        }

        return cardData
    }

    override fun endWatch() {
        isSessionOver = true
        emv.stopEmvProcess()
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        if(!userCancel && !isSessionOver){
            userCancel = true
            deviceClose()
            onEventChange(CardReaderEvent.REMOVED)
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

    private fun setEmvTransactionDataExt(){
        val config = Bundle()
        config.putInt(EmvConstant.EmvTransDataConstants.KERNEL_MODE, 0x01)
        config.putInt(EmvConstant.EmvTransDataConstants.TRANS_TYPE, TransactionType.Purchase.ordinal)
        config.putStringArrayList(EmvConstant.EmvTerminalConstraints.CONFIG , arrayListOf("DF81180170", "DF81190118", "DF811B0130"))
        emv.setTransDataConfigExt(config)
    }

    private suspend fun detectCard(): CardReaderEvent {
        println("CHECK-POINT - Here to detect the card")
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }
            val ret = checkCard()
            if(ret == CardReaderEvent.NFC){
                val powerOn = true

                if(powerOn) return CardReaderEvent.NFC
            }
            else if (ret == CardReaderEvent.CHIP) {
                val powerOn = true //powerOnIcc()

                if (powerOn){
                    return CardReaderEvent.CHIP
                }

                if (!powerOn && !hybridDetected) {
                    return CardReaderEvent.CHIP_FAILURE
                }


                chipFailure = !powerOn

                updateCardWaitingProgress("ICC Failure. Please Swipe Card")
            }
        }
    }

    private suspend fun checkCard() : CardReaderEvent =
        suspendCoroutine {
            cardReader.searchCard(true, true, true, 100, object : AidlCheckCardListener.Stub() {
                override fun onFindMagCard(p0: TrackData?) {
                    it.resume(CardReaderEvent.MAG_STRIPE)
                }

                override fun onSwipeCardFail() {
                    it.resume(CardReaderEvent.HYBRID_FAILURE)
                }

                override fun onFindICCard() {
                    deviceClose()
                    it.resume(CardReaderEvent.CHIP)
                }

                override fun onFindRFCard(p0: Int) {
                    deviceClose()
                    it.resume(CardReaderEvent.NFC)
                }

                override fun onTimeout() {
                    it.resume(CardReaderEvent.Timeout)
                }

                override fun onCancelled() {
                    it.resume(CardReaderEvent.CANCELLED)
                }

                override fun onError(p0: Int) {
                    it.resume(CardReaderEvent.Timeout)
                }

            })
        }
}