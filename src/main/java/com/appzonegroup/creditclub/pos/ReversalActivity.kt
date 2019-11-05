package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.models.messaging.ReversalRequest

class ReversalActivity : CardTransactionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showReferencePage("Reversal")
    }

    override fun onReadCard(cardData: CardData) {
        previousMessage ?: return showError("Transaction not found")

        makeRequest(ReversalRequest.generate(previousMessage!!, cardData).apply {
            processingCode3 = processingCode("00")
            withParameters(parameters.parameters)
        })
    }

    override fun onSelectAccountType() {
        readCard()
    }
}

