package com.cluster.pos.card


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/12/2019.
 * Appzone Ltd
 */

typealias CardReaderEventListener = (CardReaderEvent) -> Unit
typealias CardDataListener = (CardData?) -> Unit

interface CardReader {
    suspend fun waitForCard(): CardReaderEvent

    suspend fun read(amountStr: String): CardData?

    fun endWatch()

    suspend fun onRemoveCard(onEventChange: CardReaderEventListener)
}