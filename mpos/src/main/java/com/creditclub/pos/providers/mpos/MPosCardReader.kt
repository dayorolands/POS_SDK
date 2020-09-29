package com.creditclub.pos.providers.mpos

import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.card.CardReaderEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosCardReader(private val posManager: MPosManager, dialogProvider: DialogProvider) :
    CardReader, DialogProvider by dialogProvider {

    override suspend fun waitForCard(): CardReaderEvent {
        return requireDevice {
            posManager.connectionManager.swipeCard(4000, 0)
        }
    }

    override suspend fun read(amountStr: String): CardData? = suspendCoroutine { continuation ->
//        if (posManager.connectionManager.isBTConnected) {
//            posManager.connectionManager.swipeCard(0, amountStr.toAmountLong())
//        } else {
//            posManager.findDevice {
//                onSubmit {
//                    posManager.connectionManager.swipeCard(0, amountStr.toAmountLong())
//                }
//
//                onClose {
//                    onReadCard(MPosCardData(-1, mapOf()))
//                }
//            }
//        }
        posManager.readCard {
            continuation.resume(it)
        }
    }

    override fun endWatch() {

    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {

    }

    private suspend inline fun requireDevice(crossinline block: () -> Unit): CardReaderEvent =
        suspendCoroutine {
            if (posManager.connectionManager.isBTConnected) {
                block()
            } else {
                posManager.findDevice {
                    onSubmit {
                        block()
                    }

                    onClose {
                        it.resume(CardReaderEvent.CANCELLED)
                    }
                }
            }
        }
}