package com.appzonegroup.creditclub.pos.card


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/12/2019.
 * Appzone Ltd
 */

typealias CardReaderEventListener = (CardReaderEvent) -> Unit
typealias CardDataListener = (CardData?) -> Unit

interface CardReader {
    fun waitForCard(onEventChange: CardReaderEventListener)

    fun read(amountStr: String, onReadCard: CardDataListener)

    fun endWatch()

    suspend fun startWatch(onEventChange: CardReaderEventListener)
}