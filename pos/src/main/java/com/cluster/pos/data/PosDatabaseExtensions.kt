package com.cluster.pos.data

import com.cluster.pos.card.getTransactionType
import com.cluster.pos.extension.*
import com.cluster.pos.models.PosTransaction
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.mask
import com.cluster.core.util.toCurrencyFormat
import com.cluster.pos.card.TransactionType
import org.jpos.iso.ISOMsg
import org.koin.core.context.GlobalContext

fun PosTransaction.Companion.create(isoMsg: ISOMsg): PosTransaction {
    val koin = GlobalContext.get()
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
        appName = "Cluster POS v1.0.1",
        ptsp = "3GEE PAY",
        website = "https://www.appzonegroup.com/group/appzone-pos",
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
        amount = amountString?.toDoubleOrNull()?.div(100)?.toCurrencyFormat() ?: "NGN0.00",
    )
}
