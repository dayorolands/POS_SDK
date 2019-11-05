package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.models.messaging.FinancialMessage

class RefundActivity : CardTransactionActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

