package com.cluster.pos

import com.cluster.pos.card.cardIsoMsg
import com.cluster.pos.models.messaging.FinancialMessage
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType

class CashAdvanceActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.CashAdvance

    override fun onPosReady() {
        requestCard()
    }
    override fun onReadCard(cardData: CardData) {
        val request = cardIsoMsg(cardData, ::FinancialMessage) {
            processingCode3 = processingCode("01")
            withParameters(parameters.parameters)
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage()
    }
}

