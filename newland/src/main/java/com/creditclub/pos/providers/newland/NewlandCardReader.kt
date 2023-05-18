package com.creditclub.pos.providers.newland

import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardReaderEventListener
import com.cluster.pos.card.CardReaders
import com.newland.nsdk.core.api.common.ModuleType
import com.newland.nsdk.core.api.common.card.contactless.ContactlessCardInfo
import com.newland.nsdk.core.api.common.card.contactless.ContactlessCardType
import com.newland.nsdk.core.api.common.card.magcard.MagCardInfo
import com.newland.nsdk.core.api.common.cardreader.CardReaderListener
import com.newland.nsdk.core.api.common.cardreader.CardReaderParameters
import com.newland.nsdk.core.api.common.cardreader.CardType
import com.newland.nsdk.core.api.internal.NSDKModuleManager
import com.newland.nsdk.core.api.internal.cardreader.CardReader
import com.newland.nsdk.core.api.internal.emvl2.type.EmvConst
import com.newland.sdk.emvl3.api.common.EmvL3Const
import com.newland.sdk.emvl3.api.internal.EmvL3
import com.newland.sdk.emvl3.internal.transaction.EmvL3Impl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewlandCardReader(
    private val ordaActivity: CreditClubActivity,
    private val emvL3: EmvL3,
    private val nsdkModuleManager: NSDKModuleManager,
    private val sessionData: PosManager.SessionData
) : CardReaders, KoinComponent{
    private val cardReader = nsdkModuleManager.getModule(ModuleType.CARD_READER) as CardReader
    private val dialogProvider = ordaActivity.dialogProvider
    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED
    private var cardInterface = 0
    private var userCancel = false
    private var isSessionOver = false

    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar("Opening device", "Please wait...", true) {
            onClose {
                userCancel = true
                deviceClose()
            }
        }
        updateCardWaitingProgress("Insert or Swipe Card")
        cardReaderEvent = withContext(Dispatchers.Unconfined){
            detectCard()
        }
        dialogProvider.hideProgressBar()
        return cardReaderEvent
    }

    private fun deviceClose(){
        cardReader.cancelCardReader()
    }

    private suspend fun detectCard() : CardReaderEvent {
        var hybridDetected = false
        var chipFailure = false

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }

            val ret = checkCard()

            if (ret == CardReaderEvent.MAG_STRIPE) {
                if (!chipFailure) {
                    hybridDetected = true
                    updateCardWaitingProgress("Card is chip card. Please Insert Card")
                }
                else
                    return CardReaderEvent.MAG_STRIPE
            }
            else if (ret == CardReaderEvent.CHIP) {
                val powerOn = cardReader.isCardInserted

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
            val cardTypes = arrayOf(CardType.MAG_CARD, CardType.CONTACT_CARD, CardType.CONTACTLESS_CARD)
            val parameter = CardReaderParameters()
            parameter.contactlessCardTypes = arrayOf(ContactlessCardType.TYPE_F, ContactlessCardType.TYPE_A, ContactlessCardType.TYPE_B)
            cardReader.openCardReader(cardTypes, 45, parameter, object : CardReaderListener {
                override fun onTimeout() {
                    it.resume(CardReaderEvent.Timeout)
                }

                override fun onCancel() {
                    it.resume(CardReaderEvent.CANCELLED)
                }

                override fun onError(p0: Int, p1: String?) {
                    it.resume(CardReaderEvent.CANCELLED)
                }

                override fun onFindMagCard(p0: MagCardInfo?) {
                    it.resume(CardReaderEvent.MAG_STRIPE)
                }

                override fun onFindContactCard() {
                    cardInterface = EmvL3Const.CardInterface.CONTACT
                    it.resume(CardReaderEvent.CHIP)
                }

                override fun onFindContactlessCard(
                    p0: ContactlessCardType?,
                    p1: ContactlessCardInfo?
                ) {
                    it.resume(CardReaderEvent.NFC)
                }

            })
        }

    private fun updateCardWaitingProgress(text: String = "Please insert card") {
        dialogProvider.showProgressBar(text, "Waiting...", isCancellable = true) {
            onClose {
                userCancel = true
                deviceClose()
                if (!ordaActivity.isFinishing) ordaActivity.finish()
            }
        }
    }

    override suspend fun read(amountStr: String): CardData? {
        val cardData : CardData? = suspendCoroutine { continuation ->
            dialogProvider.showProgressBar("Processing", "IC card detected...") {
                onClose {
                    continuation.resume(null)
                }
            }

            val newlandEmvListener = NewlandEmvListener(emvL3, ordaActivity, sessionData, continuation)

        }

        return cardData
    }

    override fun endWatch() {
        cardReader.cancelCardReader()
    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
        withContext(Dispatchers.Default) {
            while (true) {
                if (userCancel) break
                if (isSessionOver) break
                if (!cardReader.isCardPresent) break
            }
        }

        if (!userCancel && !isSessionOver) {
            userCancel = true
            deviceClose()
            onEventChange(CardReaderEvent.REMOVED)
        }
    }
}