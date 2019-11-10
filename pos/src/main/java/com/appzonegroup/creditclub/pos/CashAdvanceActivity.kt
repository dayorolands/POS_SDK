package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.models.messaging.FinancialMessage
import com.appzonegroup.creditclub.pos.card.cardIsoMsg

class CashAdvanceActivity : CardTransactionActivity() {

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
