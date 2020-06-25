package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.models.messaging.*

class SalesCompleteActivity : CardTransactionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        previousMessage ?: return showError("Transaction not found")

        makeRequest(FinancialAdviceMessage.generate(previousMessage!!, cardData).apply {
            processingCode3 = processingCode("61")
            withParameters(parameters.parameters)
        })
    }

    override fun onSelectAccountType() {
        showReferencePage("Sales Completion")
    }
}

