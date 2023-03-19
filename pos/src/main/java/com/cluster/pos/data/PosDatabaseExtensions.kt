package com.cluster.pos.data

import com.cluster.core.data.model.AgentInfo
import com.cluster.core.util.mask
import com.cluster.pos.card.cardTransactionType
import com.cluster.pos.extension.*
import com.cluster.pos.models.PosTransaction
import org.jpos.iso.ISOMsg

fun PosTransaction.Companion.create(
    isoMsg: ISOMsg,
    institutionCode: String,
    appName: String,
    ptsp: String,
    website: String,
    bankName: String,
    cardHolder: String,
    cardType: String,
    nodeName: String?,
    responseCode: String,
    amountString: String,
): PosTransaction {
    val transactionType = cardTransactionType(isoMsg)

    return PosTransaction(
        institutionCode = institutionCode,
        appName = appName,
        ptsp = ptsp,
        website = website,
        pan = isoMsg.pan.mask(6, 4),
        merchantDetails = isoMsg.cardAcceptorNameLocation43,
        merchantId = isoMsg.cardAcceptorIdCode42,
        terminalId = isoMsg.terminalId41,
        expiryDate = isoMsg.cardExpirationDate14?.run {
            "${substring(2, 4)}/${substring(0, 2)}"
        },
        stan = isoMsg.stan11,
        retrievalReferenceNumber = isoMsg.retrievalReferenceNumber37,
        cardType = cardType,
        responseCode = responseCode,
        cardHolder = cardHolder,
        transactionType = transactionType.type,
        amount = amountString,
        bankName = bankName,
        nodeName = nodeName,
    )
}
