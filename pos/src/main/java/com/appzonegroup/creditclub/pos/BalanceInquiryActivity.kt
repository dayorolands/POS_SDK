package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.card.cardIsoMsg
import com.appzonegroup.creditclub.pos.models.messaging.AuthorizationRequest
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BalanceInquiryActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Balance

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        GlobalScope.launch(Dispatchers.Main) {
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

