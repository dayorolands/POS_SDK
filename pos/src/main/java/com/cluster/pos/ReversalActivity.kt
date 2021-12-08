package com.cluster.pos

import com.cluster.pos.models.messaging.ReversalRequest
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType

class ReversalActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Reversal

    override fun onPosReady() {
        showReferencePage("Reversal")
    }

    override fun onReadCard(cardData: CardData) {
        previousMessage ?: return showError("Transaction not found")

        makeRequest(ReversalRequest.generate(previousMessage!!, cardData).apply {
            processingCode3 = processingCode("00")
            withParameters(parameters.parameters)
            messageReasonCode56 = "4000"
        })
    }

    override fun onSelectAccountType() {
        readCard()
    }
}

