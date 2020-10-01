package com.creditclub.pos

import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.TransactionType


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
interface PosManager {
    val cardReader: CardReader
    val sessionData: SessionData

    suspend fun loadEmv()

    fun cleanUpEmv()

    open class SessionData {
        open var amount = 0L
        open var cashbackAmount = 0L
        open var pinBlock: String? = null
        open var canRunTransaction = false
        open var canManageParameters = false
        open var transactionType: TransactionType = TransactionType.Unknown
        open var getDukptConfig: ((pan: String, amount: Double) -> DukptConfig?)? = null
    }
}