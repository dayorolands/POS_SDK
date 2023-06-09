package com.cluster.pos.card

import com.cluster.pos.extension.processingCode3
import org.jpos.iso.ISOMsg

fun cardTransactionType(msg: ISOMsg): TransactionType {
    when (msg.mti) {
        "0420", "0421", "0430" -> {
            return TransactionType.Reversal
        }
        else -> {}
    }

    val transactionType = when (msg.processingCode3?.substring(0, 2)) {
        "00" -> TransactionType.Purchase
        "01" -> TransactionType.CashAdvance
        "09" -> TransactionType.CashBack
        "20" -> TransactionType.Refund
        "31" -> TransactionType.Balance
        "60" -> TransactionType.PreAuth
        "61" -> TransactionType.SalesComplete
        else -> TransactionType.Unknown
    }
    return transactionType
}
