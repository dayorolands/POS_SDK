package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.extension.processingCode3
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.TransactionType
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

fun cardTransactionType(msg: ISOMsg) = when (msg.mti) {
    "0100", "0110" -> when (msg.processingCode3?.substring(0, 2)) {
        "31" -> TransactionType.Balance
        "60" -> TransactionType.PreAuth
        else -> TransactionType.Unknown
    }
    "0200", "0210" -> when (msg.processingCode3?.substring(0, 2)) {
        "00" -> TransactionType.Purchase
        "20" -> TransactionType.Refund
        "09" -> TransactionType.CashBack
        "01" -> TransactionType.CashAdvance
        else -> TransactionType.Unknown
    }
    "0220", "0221", "0230" -> when (msg.processingCode3?.substring(0, 2)) {
        "61" -> TransactionType.SalesComplete
        else -> TransactionType.Unknown
    }
    "0420", "0421", "0430" -> TransactionType.Reversal
    else -> TransactionType.Unknown
}

fun ISOMsg.getTransactionType() = cardTransactionType(this)