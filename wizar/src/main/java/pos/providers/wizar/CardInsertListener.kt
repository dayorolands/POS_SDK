package pos.providers.wizar

import com.cloudpos.jniinterface.EMVJNIInterface
import com.cloudpos.jniinterface.IFuntionListener
import com.creditclub.pos.card.CardReaderEvent
import com.wizarpos.emvsample.constant.EMVConstant.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

internal class CardInsertListener(
    private val continuation: Continuation<CardReaderEvent>,
) : IFuntionListener {
    override fun emvProcessCallback(data: ByteArray?) {
    }

    override fun cardEventOccured(eventType: Int) {
        val cardReaderEvent: CardReaderEvent = when (eventType) {
            SMART_CARD_EVENT_INSERT_CARD -> {
                when (EMVJNIInterface.get_card_type()) {
                    CARD_CONTACT -> CardReaderEvent.CHIP
                    CARD_CONTACTLESS -> CardReaderEvent.MAG_STRIPE
                    else -> CardReaderEvent.HYBRID_FAILURE
                }
            }
            SMART_CARD_EVENT_POWERON_ERROR -> CardReaderEvent.CHIP_FAILURE
            SMART_CARD_EVENT_REMOVE_CARD -> return
            SMART_CARD_EVENT_CONTALESS_HAVE_MORE_CARD -> return
            SMART_CARD_EVENT_CONTALESS_ANTI_SHAKE -> return
            else -> CardReaderEvent.CHIP_FAILURE
        }
        continuation.resume(cardReaderEvent)
    }
}