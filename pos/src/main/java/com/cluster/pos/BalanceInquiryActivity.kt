package com.cluster.pos

import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType
import com.cluster.pos.card.applyCardData
import com.cluster.pos.extension.init
import com.cluster.pos.extension.processingCode3
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jpos.iso.ISOMsg

class BalanceInquiryActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Balance

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        mainScope.launch(Dispatchers.Main) {
            dialogProvider.showProgressBar("Receiving...")

            val request = ISOMsg().apply {
                init()
                mti = "0100"
                processingCode3 = processingCode("31")
                applyCardData(cardData)
            }

            makeRequest(request)
        }
    }

    override fun onSelectAccountType() = readCard()
}

