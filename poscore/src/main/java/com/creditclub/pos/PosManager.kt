package com.creditclub.pos

import android.content.Context
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.TransactionType
import org.koin.core.module.Module

typealias PanAmountFunction<R> = (pan: String, amount: Double) -> R

interface PosManager {
    val cardReader: CardReader
    val sessionData: SessionData

    suspend fun loadEmv()

    fun cleanUpEmv()

    open class SessionData {
        // Amount in kobo
        open var amount = 0L
        open var cashBackAmount = 0L
        open var canRunTransaction = false
        open var canManageParameters = false
        open var transactionType: TransactionType = TransactionType.Unknown
        open var getDukptConfig: PanAmountFunction<DukptConfig?>? = null
        open var getPosParameter: PanAmountFunction<PosParameter?>? = null
    }
}

interface PosManagerCompanion {
    val id: String
    val deviceType: Int
    val module: Module
    fun isCompatible(context: Context): Boolean
    fun setup(context: Context)
}