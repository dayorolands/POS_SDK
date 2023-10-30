package com.urovo.v67

import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardReaderEventListener
import com.cluster.pos.card.CardReaders
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.sdk.insertcard.InsertCardHandlerImpl
import com.urovo.sdk.insertcard.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.util.*
import kotlin.coroutines.suspendCoroutine

class UrovoCardReader(
    private val activity: CreditClubActivity,
    private val sessionData: PosManager.SessionData,
    private val emvNfcKernelApi: EmvNfcKernelApi
) : CardReaders, KoinComponent {
    private val dialogProvider = activity.dialogProvider
    private var cardReaderEvent: CardReaderEvent = CardReaderEvent.CANCELLED

    private val cardReader = InsertCardHandlerImpl.getInstance()
    private val cardType = Constant.Mode.MODE_USER
    private var supportsChip = true

    private var userCancel = false
    private var isSessionOver = false


    override suspend fun waitForCard(): CardReaderEvent {
        dialogProvider.showProgressBar(
            title = "Please insert card",
            message = "Waiting...",
            isCancellable = true,
        ) {
            onClose {
                userCancel = true
                if (!activity.isFinishing) activity.finish()
            }
        }

        cardReaderEvent = withContext(Dispatchers.Unconfined){
            detectCard()
        }
        dialogProvider.hideProgressBar()

        return cardReaderEvent
    }

    override suspend fun read(amountStr: String): CardData? {
        TODO("Not yet implemented")
    }

    override fun endWatch() {

    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {

    }

    private fun detectCard(): CardReaderEvent {
        val atrData = ByteArray(64)

        while (true) {
            if (userCancel) {
                return CardReaderEvent.CANCELLED
            }
            if(supportsChip){
                val ret = cardReader.powerUp(cardType, atrData)

                if (ret > 0 && cardReader.isCardIn) {
                    return CardReaderEvent.CHIP
                }
                else if (ret < 0) {
                    updateCardWaitingProgress("ICC Failure. Please Swipe Card")
                    return CardReaderEvent.CHIP_FAILURE
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
    }
}