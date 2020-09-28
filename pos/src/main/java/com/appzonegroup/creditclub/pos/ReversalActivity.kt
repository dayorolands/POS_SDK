package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.models.messaging.ReversalRequest
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.TransactionType

class ReversalActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Reversal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

