package com.cluster.pos.card

import com.cluster.pos.extension.processingCode3
import org.jpos.iso.ISOMsg


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
inline fun <T : CardIsoMsg> cardIsoMsg(
    cardData: CardData,
    crossinline factory: () -> T,
    crossinline block: T.() -> Unit,
): T {
    return factory().apply {
        init()
        apply(cardData)
        apply(block)
    }
}

fun cardTransactionType(msg: ISOMsg): TransactionType {
    val transactionType = when (msg.processingCode3?.substring(0, 2)) {
        "00" -> TransactionType.Purchase
        "01" -> TransactionType.CashAdvance
        "09" -> TransactionType.CashBack
        "20" -> TransactionType.Refund
        "31" -> TransactionType.Balance
        "60" -> TransactionType.PreAuth
        "61" -> TransactionType.SalesComplete
        else -> when (msg.mti) {
            "0420", "0421", "0430" -> TransactionType.Reversal
            else -> TransactionType.Unknown
        }
    }
    return transactionType
}
