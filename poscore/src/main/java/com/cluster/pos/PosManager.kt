package com.cluster.pos

import android.content.Context
import com.cluster.pos.card.CardReaders
import com.cluster.pos.card.TransactionType
import org.koin.core.module.Module

typealias PanAmountFunction<R> = () -> R

interface PosManager {
    val cardReader: CardReaders
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

    companion object {
        const val IPEK = "3F2216D8297BCE9C"
        const val KSN = "0000000002DDDDE00001"
        const val KsnCounter = "0"
    }
}

interface PosManagerCompanion {
    val id: String
    val deviceType: Int
    val module: Module
    fun isCompatible(context: Context): Boolean
    fun setup(context: Context)
}