package com.creditclub.pos.providers.sunmi

import android.os.Bundle
import android.os.RemoteException
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.debug
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosManager
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.card.CardReaderEventListener
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import sunmi.paylib.SunmiPayKernel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/12/2019.
 * Appzone Ltd
 */
class SunmiCardReader(
    private val activity: CreditClubActivity,
    private val kernel: SunmiPayKernel,
    private val sessionData: PosManager.SessionData,
) :
    CardReader,
    KoinComponent {
    private var cardReader = kernel.mReadCardOptV2
    private var emv = kernel.mEMVOptV2
    private val dialogProvider = activity.dialogProvider

    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED

    private var userCancel = false
    private var isSessionOver = false

    private var allowedCardType: Int =
        AidlConstants.CardType.MAGNETIC.value or AidlConstants.CardType.IC.value
    private var selectedCardType = 0

    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar("Opening device", "Please wait...", true) {
            onClose {
                userCancel = true
                deviceClose()
            }
        }
        emv.initEmvProcess()
        updateCardWaitingProgress("Insert or swipe card")
        cardReaderEvent = withContext(Dispatchers.IO) { detectCard() }

        dialogProvider.hideProgressBar()

        return cardReaderEvent
    }

    override suspend fun read(amountStr: String): CardData? {
        val cardData: CardData? = suspendCoroutine { continuation ->
            dialogProvider.showProgressBar("Processing", "IC card detected...") {
                onClose {
//                    emv.emvProcessCancel()
                    continuation.resume(null)
                }
            }
            val emvListener = SunmiEmvListener(activity, kernel, sessionData, continuation)
            emv.setTlv(AidlConstants.EMV.TLVOpCode.OP_NORMAL, "9F33", "E040C8")

            val emvTransData = EMVTransDataV2()
            emvTransData.amount = sessionData.amount.toString()
            emvTransData.flowType = 1
            emvTransData.cardType = selectedCardType
            emv.transactProcess(emvTransData, emvListener)
        }
        dialogProvider.hideProgressBar()
        return cardData
    }

    override fun endWatch() {
        isSessionOver = true
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
//        withContext(Dispatchers.IO) {
//            while (true) {
//                if (userCancel) break
//                if (isSessionOver) break
//                if (cardReader.getCardExistStatus(AidlConstants.CardType.IC.value) != 2) break
//            }
//        }
//
//        if (!userCancel && !isSessionOver) {
//            userCancel = true
//            deviceClose()
//            onEventChange(CardReaderEvent.REMOVED)
//        }
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

    private fun deviceClose() {
        safeRun { cardReader.cancelCheckCard() }
//        cardReader.cardOff(allowedCardType)
    }

    private suspend fun detectCard(): CardReaderEvent {
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }

            val ret = checkCard(allowedCardType, 60)

            if (ret == CardReaderEvent.MAG_STRIPE) {
                if (!chipFailure) {
                    hybridDetected = true

                    updateCardWaitingProgress("Card is chip card. Please Insert Card")
                } else return CardReaderEvent.MAG_STRIPE
            } else if (ret == CardReaderEvent.CHIP) {
                val powerOn = true//powerOnIcc()

                if (powerOn) return CardReaderEvent.CHIP

//                    hybridDetected = true

                if (!powerOn && !hybridDetected) {
                    return CardReaderEvent.CHIP_FAILURE
                }

                chipFailure = !powerOn

                updateCardWaitingProgress("ICC Failure. Please Swipe Card")
            }
        }
    }

    private suspend fun checkCard(cardType: Int, timeout: Int): CardReaderEvent =
        suspendCoroutine {
            cardReader.checkCard(cardType, object : CheckCardCallbackV2.Stub() {
                @Throws(RemoteException::class)
                override fun findMagCard(bundle: Bundle) {
                    debug("findMagCard:$bundle")
                    selectedCardType = AidlConstants.CardType.MAGNETIC.value
                    it.resume(CardReaderEvent.MAG_STRIPE)
                }

                @Throws(RemoteException::class)
                override fun findICCard(atr: String) {
                    debug("findICCard:$atr")
                    selectedCardType = AidlConstants.CardType.IC.value
                    it.resume(CardReaderEvent.CHIP)
                }

                @Throws(RemoteException::class)
                override fun findRFCard(uuid: String) {
                    debug("findRFCard:$uuid")
                    selectedCardType = AidlConstants.CardType.NFC.value
                    it.resume(CardReaderEvent.NFC)
                }

                @Throws(RemoteException::class)
                override fun onError(code: Int, message: String) {
                    safeRun { throw RuntimeException("Sunmi check card error $message -- $code") }
                    dialogProvider.hideProgressBar()
                    it.resume(CardReaderEvent.Timeout)
                }
            }, timeout)
        }
}
