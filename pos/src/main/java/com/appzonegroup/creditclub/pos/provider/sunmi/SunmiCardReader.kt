package com.appzonegroup.creditclub.pos.provider.sunmi

import com.appzonegroup.creditclub.pos.card.CardDataListener
import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.CardReaderEventListener


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/12/2019.
 * Appzone Ltd
 */
class SunmiCardReader : CardReader {
    override fun waitForCard(onEventChange: CardReaderEventListener) {

    }

    override fun read(amountStr: String, onReadCard: CardDataListener) {

    }

    override fun endWatch() {

    }

    override suspend fun startWatch(onEventChange: CardReaderEventListener) {

    }
}