package com.cluster.pos

import com.cluster.pos.card.cardIsoMsg
import com.cluster.pos.models.messaging.AuthorizationRequest
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BalanceInquiryActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Balance

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        mainScope.launch(Dispatchers.Main) {
            dialogProvider.showProgressBar("Receiving...")

            val request = cardIsoMsg(cardData, ::AuthorizationRequest) {
                processingCode3 = processingCode("31")
                withParameters(parameters.parameters)
            }

            makeRequest(request)
        }
    }

    override fun onSelectAccountType() = readCard()
}

