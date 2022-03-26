package com.cluster.pos

import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType
import com.cluster.pos.card.generateReversal
import com.cluster.pos.extension.messageReasonCode56
import com.cluster.pos.extension.processingCode3

class ReversalActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Reversal

    override fun onPosReady() {
        showReferencePage("Reversal")
    }

    override fun onReadCard(cardData: CardData) {
        previousMessage ?: return showError("Transaction not found")

        val request = previousMessage!!.generateReversal(cardData).apply {
            processingCode3 = processingCode("00")
            messageReasonCode56 = "4000"
        }
        makeRequest(request)
    }

    override fun onSelectAccountType() {
        readCard()
    }
}

