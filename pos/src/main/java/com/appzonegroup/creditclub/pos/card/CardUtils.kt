package com.appzonegroup.creditclub.pos.card

import com.appzonegroup.creditclub.pos.extension.processingCode3
import org.jpos.iso.ISOMsg


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
fun <T : CardIsoMsg> cardIsoMsg(cardData: CardData, factory: () -> T, block: (T.() -> Unit)? = null): T {
    return factory().apply {
        init()
        apply(cardData)
        block?.also {
            apply(block)
        }
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
    "0420", "421", "0430" -> TransactionType.Reversal
    else -> TransactionType.Unknown
}

fun maskPan(pan: String?) = pan.run {
    this ?: return@run "***"

    if (length > 6) "${substring(0, 6)}${"*".repeat(length - 6)}"
    else this
}