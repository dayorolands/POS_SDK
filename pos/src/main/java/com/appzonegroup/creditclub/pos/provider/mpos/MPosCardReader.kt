package com.appzonegroup.creditclub.pos.provider.mpos

import com.appzonegroup.creditclub.pos.card.CardDataListener
import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.CardReaderEventListener


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class MPosCardReader : CardReader {
    override fun waitForCard(onEventChange: CardReaderEventListener) {

    }

    override fun read(amountStr: String, onReadCard: CardDataListener) {

    }

    override fun endWatch() {

    }

    override suspend fun startWatch(onEventChange: CardReaderEventListener) {

    }
}