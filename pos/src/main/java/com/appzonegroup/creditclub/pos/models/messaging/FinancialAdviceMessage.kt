package com.appzonegroup.creditclub.pos.models.messaging

import com.cluster.pos.card.CardData
import com.appzonegroup.creditclub.pos.card.CardIsoMsg
import com.appzonegroup.creditclub.pos.card.cardIsoMsg
import com.appzonegroup.creditclub.pos.extension.acquiringInstIdCode32
import com.appzonegroup.creditclub.pos.extension.forwardingInstIdCode33
import com.appzonegroup.creditclub.pos.extension.transactionFee28
import org.jpos.iso.ISOException
import org.json.JSONException
import java.io.IOException
import java.security.NoSuchAlgorithmException

open class FinancialAdviceMessage : CardIsoMsg() {
    var originalDataElements: String?
        get() = getString(90)
        set(value) = set(90, value)

    var replacementAmounts: String?
        get() = getString(95)
        set(value) = set(95, value)

    override fun init() {
        super.init()
        mti = "0220"
        processingCode3 = "000000"
    }

    @Throws(
        ISOException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        JSONException::class
    )
    override fun apply(data: CardData): FinancialAdviceMessage {
        super.apply(data)

        return this
    }


    companion object {
        fun generate(financialResponse: BaseIsoMsg, cardData: CardData): FinancialAdviceMessage {
            return generate(financialResponse.convert(::AuthorizationRequest), cardData)
        }

        fun generate(
            financialMessage: AuthorizationRequest,
            cardData: CardData
        ): FinancialAdviceMessage {
            val elements = financialMessage.run {
                "0100$stan11$transmissionDateTime7${padZeros(acquiringInstIdCode32, 11)}${
                    padZeros(
                        forwardingInstIdCode33,
                        11
                    )
                }"
            }

            return cardIsoMsg(cardData, ::FinancialAdviceMessage) {
                transactionAmount4 = financialMessage.transactionAmount4
                replacementAmounts =
                    "${padZeros(transactionAmount4, 12)}000000000000${
                        padZeros(
                            transactionFee28,
                            9
                        )
                    }000000000"

                originalDataElements = elements
                stan11 = financialMessage.stan11
                localTransactionDate13 = financialMessage.localTransactionDate13
                localTransactionTime12 = financialMessage.localTransactionTime12
                retrievalReferenceNumber37 = financialMessage.retrievalReferenceNumber37
//                messageReasonCode56 = "4000"
            }
        }
    }
}
