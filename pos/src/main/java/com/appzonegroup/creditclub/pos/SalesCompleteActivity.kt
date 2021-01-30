package com.appzonegroup.creditclub.pos

import com.appzonegroup.creditclub.pos.models.messaging.FinancialAdviceMessage
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.TransactionType

class SalesCompleteActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.SalesComplete

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showReferencePage("Sales Completion")
    }

    override fun onReadCard(cardData: CardData) {
        previousMessage ?: return showError("Transaction not found")

        makeRequest(FinancialAdviceMessage.generate(previousMessage!!, cardData).apply {
            processingCode3 = processingCode("61")
            withParameters(parameters.parameters)
        })
    }

    override fun onSelectAccountType() {
        showAmountPage("Amount") { amount ->
            viewModel.amountString.value = amount.toString()
            sessionData.amount = amount
            readCard()
        }
    }
}

