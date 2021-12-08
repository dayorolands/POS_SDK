package com.appzonegroup.creditclub.pos.models.messaging

import com.cluster.pos.card.CardData
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

    @Throws(
        ISOException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        JSONException::class
    )
    override fun apply(data: CardData): FinancialMessage {
        super.apply(data)

        return this
    }

    fun generateRefund(cardData: CardData) =
        cardIsoMsg(cardData, ::FinancialMessage) {
            transactionAmount4 = transactionAmount4
            stan11 = stan11
            localTransactionDate13 = localTransactionDate13
            localTransactionTime12 = localTransactionTime12
            retrievalReferenceNumber37 = retrievalReferenceNumber37
            messageReasonCode56 = "4000"
        }
}
