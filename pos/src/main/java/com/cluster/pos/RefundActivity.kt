package com.cluster.pos

import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType
import com.cluster.pos.card.applyCardData
import com.cluster.pos.extension.*
import com.cluster.pos.util.ISO87Packager
import org.jpos.iso.ISOMsg

class RefundActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Refund

    override fun onPosReady() {
        showReferencePage("Refund")
    }

    override fun onReadCard(cardData: CardData) {
        val request = generateRefund(isoMsg = previousMessage!!, cardData = cardData).apply {
            processingCode3 = processingCode("20")
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage()
    }

    private fun generateRefund(isoMsg: ISOMsg, cardData: CardData): ISOMsg {
        return ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0200"
            processingCode3 = "000000"
            transactionAmount4 = isoMsg.transactionAmount4
            stan11 = isoMsg.stan11
            localTransactionDate13 = isoMsg.localTransactionDate13
            localTransactionTime12 = isoMsg.localTransactionTime12
            retrievalReferenceNumber37 = isoMsg.retrievalReferenceNumber37
            messageReasonCode56 = "4000"
            applyCardData(cardData)
        }
    }
}

