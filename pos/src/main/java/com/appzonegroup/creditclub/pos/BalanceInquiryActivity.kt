package com.appzonegroup.creditclub.pos

import android.os.Bundle
import com.appzonegroup.creditclub.pos.models.messaging.AuthorizationRequest
import com.appzonegroup.creditclub.pos.card.CardData
import com.appzonegroup.creditclub.pos.card.cardIsoMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BalanceInquiryActivity : CardTransactionActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        GlobalScope.launch(Dispatchers.Main) {
            showProgressBar("Receiving...")

            val request = cardIsoMsg(cardData, ::AuthorizationRequest) {
                processingCode3 = processingCode("31")
                withParameters(parameters.parameters)
            }

            makeRequest(request)
        }
    }

    override fun onSelectAccountType() = readCard()
}

