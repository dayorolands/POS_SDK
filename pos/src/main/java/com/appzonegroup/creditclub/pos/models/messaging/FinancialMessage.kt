package com.appzonegroup.creditclub.pos.models.messaging

import com.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.card.CardIsoMsg
import com.appzonegroup.creditclub.pos.card.cardIsoMsg
import org.jpos.iso.ISOException
import org.json.JSONException
import java.io.IOException
import java.security.NoSuchAlgorithmException

open class FinancialMessage : CardIsoMsg() {
    init {
        mti = "0200"
    }

    override fun init() {
        super.init()
        mti = "0200"
        processingCode3 = "000000"
    }

    @Throws(ISOException::class, IOException::class, NoSuchAlgorithmException::class, JSONException::class)
    override fun apply(data: CardData): FinancialMessage {
        super.apply(data)

        return this
    }

    fun generateRefund(cardData: CardData): FinancialMessage {
        val refundMsg = cardIsoMsg(cardData, ::FinancialMessage)

        refundMsg.transactionAmount4 = transactionAmount4
        refundMsg.stan11 = stan11
        refundMsg.localTransactionDate13 = localTransactionDate13
        refundMsg.localTransactionTime12 = localTransactionTime12
        refundMsg.retrievalReferenceNumber37 = retrievalReferenceNumber37
        refundMsg.messageReasonCode56 = "4000"

        return refundMsg
    }
}
