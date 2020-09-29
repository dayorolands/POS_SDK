package com.creditclub.pos.providers.sunmi

import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.card.CardReaderEventListener
import kotlinx.coroutines.delay


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/12/2019.
 * Appzone Ltd
 */
class SunmiCardReader : CardReader {
    override suspend fun waitForCard(): CardReaderEvent {
        delay(3000)
        return CardReaderEvent.CANCELLED
    }

    override suspend fun read(amountStr: String): CardData? {
        return null
    }

    override fun endWatch() {

    }

    override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {

    }
}