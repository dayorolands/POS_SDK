package com.appzonegroup.creditclub.pos

import com.appzonegroup.creditclub.pos.card.cardIsoMsg
import com.appzonegroup.creditclub.pos.models.messaging.FinancialMessage
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType

class CardWithdrawalActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Purchase

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        val request = cardIsoMsg(cardData, ::FinancialMessage) {
            processingCode3 = processingCode("00")
            withParameters(parameters.parameters)
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage()
    }
}

