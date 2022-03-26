package com.cluster.pos

import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType
import com.cluster.pos.card.applyCardData
import com.cluster.pos.extension.processingCode3
import com.cluster.pos.util.ISO87Packager
import org.jpos.iso.ISOMsg

class CardWithdrawalActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.Purchase

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        val request = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0200"
            processingCode3 = processingCode("00")
            applyCardData(cardData)
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage()
    }
}

