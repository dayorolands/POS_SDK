package com.appzonegroup.creditclub.pos.provider.mpos

import com.appzonegroup.creditclub.pos.card.CardDataListener
import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.CardReaderEvent
import com.appzonegroup.creditclub.pos.card.CardReaderEventListener
import com.creditclub.core.ui.widget.DialogProvider


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosCardReader(private val posManager: MPosManager, dialogProvider: DialogProvider) :
    CardReader, DialogProvider by dialogProvider {

    override fun waitForCard(onEventChange: CardReaderEventListener) {
        requireDevice(onEventChange) {
            posManager.connectionManager.swipeCard(4000, 0)
        }
    }

    override fun read(amountStr: String, onReadCard: CardDataListener) {
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
        posManager.readCard(onReadCard)
    }

    override fun endWatch() {

    }

    override suspend fun startWatch(onEventChange: CardReaderEventListener) {

    }

    private inline fun requireDevice(
        crossinline onEventChange: CardReaderEventListener,
        crossinline block: () -> Unit
    ) {
        if (posManager.connectionManager.isBTConnected) {
            block()
        } else {
            posManager.findDevice {
                onSubmit {
                    block()
                }

                onClose {
                    onEventChange(CardReaderEvent.CANCELLED)
                }
            }
        }
    }

    private fun String.toAmountLong(): Long {
        return (substring(3).toDouble() * 100).toLong()
    }
}