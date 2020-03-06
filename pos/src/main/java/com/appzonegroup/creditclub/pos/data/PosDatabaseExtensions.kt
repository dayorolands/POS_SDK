package com.appzonegroup.creditclub.pos.data

import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.card.cardTransactionType
import com.appzonegroup.creditclub.pos.card.maskPan
import com.appzonegroup.creditclub.pos.extension.*
import com.appzonegroup.creditclub.pos.models.PosTransaction
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.data.prefs.LocalStorage
import org.jpos.iso.ISOMsg
import org.koin.core.KoinComponent
import org.koin.core.get


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/02/2020.
 * Appzone Ltd
 */

private inline val KoinComponent.localStorage get() = get<LocalStorage>()
private inline val KoinComponent.agent get() = localStorage.agent

fun PosTransaction.Companion.create(isoMsg: ISOMsg): PosTransaction {

    return PosTransaction().apply {
        institutionCode = localStorage.institutionCode
        agentPhoneNumber = localStorage.agent?.phoneNumber
        agentName = agent?.agentName
        agentCode = agent?.agentCode
        appName = "CreditClub POS v${BuildConfig.VERSION_NAME}"
        ptsp = "3GEE PAY"
        website = "http://www.appzonegroup.com/products/creditclub"
        pan = maskPan(isoMsg.pan)

        merchantDetails = isoMsg.cardAcceptorNameLocation43
        merchantId = isoMsg.cardAcceptorIdCode42
        terminalId = isoMsg.terminalId41
        expiryDate = isoMsg.cardExpirationDate14?.run {
            "${substring(2, 4)}/${substring(0, 2)}"
        }
        stan = isoMsg.stan11
        retrievalReferenceNumber = isoMsg.retrievalReferenceNumber37
//                cardType = cardType
        responseCode = isoMsg.responseCode39

//                cardHolder = cardHolder
        transactionType = cardTransactionType(isoMsg).type

        amount =
            if (cardHolder == "BALANCE INQUIRY") CurrencyFormatter.format(isoMsg.additionalAmounts54)
            else CurrencyFormatter.format(isoMsg.transactionAmount4)
    }
}
