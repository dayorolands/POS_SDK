package com.cluster.pos

import com.cluster.pos.card.cardIsoMsg
import com.cluster.pos.models.messaging.AuthorizationRequest
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType

class PreAuthActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.PreAuth

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        val request = cardIsoMsg(cardData, ::AuthorizationRequest) {
            processingCode3 = processingCode("60")
            withParameters(parameters.parameters)
//            posConditionCode25 = "06"
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage()
    }
}

