package com.appzonegroup.creditclub.pos

import com.appzonegroup.creditclub.pos.models.messaging.FinancialMessage
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType

class RefundActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Refund

    override fun onPosReady() {
        showReferencePage("Refund")
    }

    override fun onReadCard(cardData: CardData) {
        val request = previousMessage!!.convert(::FinancialMessage).generateRefund(cardData).apply {
//        val request = cardIsoMsg(cardData, ::FinancialMessage) {
            processingCode3 = processingCode("20")
            withParameters(parameters.parameters)
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage()
    }
}

