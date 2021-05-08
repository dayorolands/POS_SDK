package com.appzonegroup.creditclub.pos.data

import com.appzonegroup.creditclub.pos.card.getTransactionType
import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.models.PosTransaction
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.pos.card.TransactionType
import org.jpos.iso.ISOMsg
import org.koin.core.context.GlobalContext

fun PosTransaction.Companion.create(isoMsg: ISOMsg): PosTransaction {
    val koin = GlobalContext.get().koin
    val localStorage = koin.get<LocalStorage>()
    val agent = localStorage.agent
    val transactionType = isoMsg.getTransactionType()
    val amountString =
        if (transactionType == TransactionType.Balance) isoMsg.additionalAmounts54
        else isoMsg.transactionAmount4

    return PosTransaction(
        institutionCode = localStorage.institutionCode,
        agentPhoneNumber = agent?.phoneNumber,
        agentName = agent?.agentName,
        agentCode = agent?.agentCode,
        appName = "CreditClub POS v1.0.1",
        ptsp = "3GEE PAY",
        website = "http://www.appzonegroup.com/products/creditclub",
        pan = isoMsg.pan.mask(6, 4),
        merchantDetails = isoMsg.cardAcceptorNameLocation43,
        merchantId = isoMsg.cardAcceptorIdCode42,
        terminalId = isoMsg.terminalId41,
        expiryDate = isoMsg.cardExpirationDate14?.run {
            "${substring(2, 4)}/${substring(0, 2)}"
        },
        stan = isoMsg.stan11,
        retrievalReferenceNumber = isoMsg.retrievalReferenceNumber37,
//                cardType = isoMsg.cardType,
        responseCode = isoMsg.responseCode39,
//                cardHolder = isoMsg.cardHolder,
        transactionType = transactionType.type,
        amount = amountString?.toLongOrNull()?.toCurrencyFormat() ?: "NGN0.00",
    )
}
