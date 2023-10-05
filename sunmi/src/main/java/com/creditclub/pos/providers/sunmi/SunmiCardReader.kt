package com.creditclub.pos.providers.sunmi

import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debug
import com.cluster.core.util.safeRun
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardReaderEventListener
import com.cluster.pos.card.CardReaders
import com.sunmi.pay.hardware.aidl.AidlConstants
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import sunmi.paylib.SunmiPayKernel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class SunmiCardReader(
    private val activity: CreditClubActivity,
    private val kernel: SunmiPayKernel,
    private val sessionData: PosManager.SessionData,
) : CardReaders, KoinComponent {
    private var cardReader = kernel.mReadCardOptV2
    private var emv = kernel.mEMVOptV2
    private val dialogProvider = activity.dialogProvider

    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED

    private var userCancel = false
    private var isSessionOver = false

    private var allowedCardType: Int =
        AidlConstants.CardType.MAGNETIC.value or AidlConstants.CardType.IC.value or AidlConstants.CardType.NFC.value
    private var selectedCardType = 0

    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar("Opening device", "Please wait...", true) {
            onClose {
                userCancel = true
                deviceClose()
            }
        }
        emv.abortTransactProcess()
        emv.initEmvProcess()
        updateCardWaitingProgress("Insert or Tap card")
        cardReaderEvent = withContext(Dispatchers.Unconfined) {
            detectCard()
        }
        dialogProvider.hideProgressBar()
        return cardReaderEvent
    }

    override suspend fun read(amountStr: String): CardData? {
        val cardData: CardData? = suspendCoroutine { continuation ->

            Log.d("DetectCard", "Time to read the card")

            dialogProvider.showProgressBar("Processing", "IC card detected...") {
                onClose {
                    continuation.resume(null)
                }
            }
            Log.d("DetectCard", "The selected card is : $selectedCardType")

            val emvListener = SunmiEmvListener(activity, kernel, sessionData, continuation)
            emv.setTlv(AidlConstants.EMV.TLVOpCode.OP_NORMAL, "9F33", "E0F0C8")
            val emvTransData = EMVTransDataV2()
            emvTransData.amount = sessionData.amount.toString()
            if(selectedCardType == AidlConstants.CardType.NFC.value) {
                emvTransData.flowType = AidlConstants.EMV.FlowType.TYPE_NFC_SPEEDUP
            } else {
                emvTransData.flowType = AidlConstants.EMV.FlowType.TYPE_EMV_STANDARD
            }
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
        if (!userCancel && !isSessionOver) {
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

    private fun deviceClose() {
        safeRun { cardReader.cancelCheckCard() }
    }

    private suspend fun detectCard(): CardReaderEvent {
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }
            initEmvTlvData()
            val ret = checkCard(allowedCardType, 60)

            if (ret == CardReaderEvent.Timeout){
                return CardReaderEvent.Timeout
            }
            else if (ret == CardReaderEvent.MAG_STRIPE) {
                if (!chipFailure) {
                    hybridDetected = true
                    updateCardWaitingProgress("Card is chip card. Please Insert Card")
                }
                else
                    return CardReaderEvent.MAG_STRIPE
            }
            else if(ret == CardReaderEvent.NFC){
                val powerOn = true

                if(powerOn) return CardReaderEvent.NFC
            }
            else if (ret == CardReaderEvent.CHIP) {
                val powerOn = true

                if (powerOn) return CardReaderEvent.CHIP

                if (!powerOn && !hybridDetected) {
                    return CardReaderEvent.CHIP_FAILURE
                }

                chipFailure = !powerOn

                updateCardWaitingProgress("ICC Failure. Please Swipe Card")
            }
        }
    }

    /**
     * Set tlv essential tlv data
     */
    private fun initEmvTlvData() {
        try {
            // set PayPass(MasterCard) tlv data
            val tagsPayPass = arrayOf(
                "DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C",
                "DF8123", "DF8124", "DF8125", "DF8126",
                "DF811B", "DF811D", "DF8122", "DF8120", "DF8121"
            )
            val valuesPayPass = arrayOf(
                "E0", "F8", "F8", "E8", "00", "00",
                "000000000000", "000000100000", "999999999999", "000000100000",
                "30", "02", "0000000000", "000000000000", "000000000000"
            )
            emv.setTlvList(
                AidlConstants.EMV.TLVOpCode.OP_PAYPASS,
                tagsPayPass,
                valuesPayPass
            )

            //set Visa tlv data
            val tagsPayWave = arrayOf(
                "DF8124", "DF8125", "DF8126"
            )
            val valuesPayWave = arrayOf(
                "999999999999", "999999999999", "000000000000"
            )
            emv.setTlvList(
                AidlConstants.EMV.TLVOpCode.OP_PAYWAVE,
                tagsPayWave,
                valuesPayWave
            )

            // set AMEX(AmericanExpress) tlv data
            val tagsAE = arrayOf(
                "9F6D", "9F6E", "9F33", "9F35",
                "DF8168", "DF8167", "DF8169", "DF8170"
                )
            val valuesAE = arrayOf(
                "C0", "D8E00000", "E0E888",
                "22", "00", "00", "00", "60"
            )
            emv.setTlvList(AidlConstants.EMV.TLVOpCode.OP_AE, tagsAE, valuesAE)

            //set JCB tlv data
            val tagsJCB = arrayOf("9F53", "DF8161")
            val valuesJCB = arrayOf("708000", "7F00")
            emv.setTlvList(AidlConstants.EMV.TLVOpCode.OP_JCB, tagsJCB, valuesJCB)
        } catch (e: RemoteException) {
            e.printStackTrace()
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
                }

                @Throws(RemoteException::class)
                override fun findRFCard(uuid: String) {
                    debug("findRFCard:$uuid")
                    selectedCardType = AidlConstants.CardType.NFC.value
                }

                @Throws(RemoteException::class)
                override fun onError(code: Int, message: String) {
                    safeRun { throw RuntimeException("Sunmi check card error $message -- $code") }
                    it.resume(CardReaderEvent.Timeout)
                }

                @Throws(RemoteException::class)
                override fun findICCardEx(atr: Bundle?) {
                    debug("findICCardEx:$atr")
                    it.resume(CardReaderEvent.CHIP)
                }

                @Throws(RemoteException::class)
                override fun findRFCardEx(bundle: Bundle?) {
                    debug("findRFCardEx:$bundle")
                    it.resume(CardReaderEvent.NFC)
                }

                @Throws(RemoteException::class)
                override fun onErrorEx(bundle: Bundle?) {
                    safeRun { throw RuntimeException("Sunmi check card error $bundle") }
                    it.resume(CardReaderEvent.Timeout)
                }

            }, timeout)
        }
}
