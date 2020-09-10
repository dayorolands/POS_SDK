package com.appzonegroup.creditclub.pos.models.messaging

import com.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.card.CardIsoMsg
import com.appzonegroup.creditclub.pos.card.cardIsoMsg
import com.appzonegroup.creditclub.pos.extension.*
import org.jpos.iso.ISOException
import org.jpos.iso.ISOMsg
import org.json.JSONException
import java.io.IOException
import java.security.NoSuchAlgorithmException

class ReversalRequest : CardIsoMsg() {
    var originalDataElements: String?
        get() = getString(90)
        set(value) = set(90, value)

    var replacementAmounts: String?
        get() = getString(95)
        set(value) = set(95, value)

    override fun init() {
        super.init()
        mti = "0420"
    }

    @Throws(ISOException::class, IOException::class, NoSuchAlgorithmException::class, JSONException::class)
    override fun apply(data: CardData): ReversalRequest {
        super.apply(data)

        return apply {
            processingCode3 = "000000"
        }
    }

    companion object {
        fun generate(financialMessage: ISOMsg, cardData: CardData): ReversalRequest {
            val elements = financialMessage.run {
                "0200$stan11$transmissionDateTime7${padZeros(acquiringInstIdCode32, 11)}${padZeros(
                    forwardingInstIdCode33,
                    11
                )}"
            }

            return cardIsoMsg(cardData, ::ReversalRequest) {
                mti = "0420"
                processingCode3 = "000000"
                transactionAmount4 = financialMessage.transactionAmount4
                replacementAmounts =
                    "${padZeros(transactionAmount4, 12)}000000000000${padZeros(transactionFee28, 9)}000000000"

                originalDataElements = elements
                stan11 = financialMessage.stan11
                localTransactionDate13 = financialMessage.localTransactionDate13
                localTransactionTime12 = financialMessage.localTransactionTime12
                retrievalReferenceNumber37 = financialMessage.retrievalReferenceNumber37
                messageReasonCode56 = "4000"
            }
        }
    }
}
