package com.creditclub.pos

import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.TransactionType

interface PosManager {
    val cardReader: CardReader
    val sessionData: SessionData

    suspend fun loadEmv()

    fun cleanUpEmv()

    open class SessionData {
        open var amount = 0L
        open var cashBackAmount = 0L
        open var canRunTransaction = false
        open var canManageParameters = false
        open var transactionType: TransactionType = TransactionType.Unknown
        open var getDukptConfig: ((pan: String, amount: Double) -> DukptConfig?)? = null
        open var getPosParameter: ((pan: String, amount: Double) -> PosParameter?)? = null
    }
}