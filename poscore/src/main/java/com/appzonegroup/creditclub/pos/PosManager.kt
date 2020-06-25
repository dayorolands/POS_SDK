package com.appzonegroup.creditclub.pos

import com.appzonegroup.creditclub.pos.card.CardReader


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
interface PosManager {
    val cardReader: CardReader
    val sessionData: SessionData

    suspend fun loadEmv()

    fun cleanUpEmv()

    class SessionData {
        var amount = 0L
        var pinBlock: String? = null
    }
}