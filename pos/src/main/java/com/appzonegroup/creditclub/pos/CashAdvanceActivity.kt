package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.card.cardIsoMsg
import com.appzonegroup.creditclub.pos.models.messaging.FinancialMessage
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.TransactionType

class CashAdvanceActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.CashAdvance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

