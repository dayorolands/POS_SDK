package com.appzonegroup.creditclub.pos.provider.sunmi

import com.appzonegroup.creditclub.pos.card.*
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