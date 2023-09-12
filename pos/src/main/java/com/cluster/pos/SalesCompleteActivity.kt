package com.cluster.pos

import android.util.Log
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType
import com.cluster.pos.card.applyCardData
import com.cluster.pos.card.applySalesCompleteData
import com.cluster.pos.extension.*
import com.cluster.pos.util.ISO87Packager
import org.jpos.iso.ISOMsg

class SalesCompleteActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.SalesComplete

    override fun onPosReady() {
        showReferencePage("Sales Completion")
    }

    override fun onReadCard(cardData: CardData) {
        previousMessage ?: return showError("Transaction not found")

        val request = generateFinancialAdvice(previousMessage!!, cardData)
        request.processingCode3 = processingCode("61")
        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage("Amount") { amount ->
            viewModel.amountString.value = amount.toString()
            sessionData.amount = amount
            readCard()
        }
    }

    private fun generateFinancialAdvice(financialMessage: ISOMsg, cardData: CardData): ISOMsg {
        val preAuthStan = viewModel.preAuthStan.value
        val elements = financialMessage.run {
            "0100$preAuthStan$transmissionDateTime7" +
                    "${acquiringInstIdCode32?.padStart(11, '0')}" +
                    "${forwardingInstIdCode33?.padStart(11, '0')}"
        }

        return ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0220"
            processingCode3 = "000000"
            applySalesCompleteData(cardData)
            transactionAmount4 = financialMessage.transactionAmount4
            replacementAmounts95 =
                "${transactionAmount4?.padStart(12, '0')}" +
                        "000000000000" +
                        "${transactionFee28?.padStart(9, '0')}" +
                        "000000000"

            originalDataElements90 = elements
            stan11 = financialMessage.stan11
            localTransactionDate13 = financialMessage.localTransactionDate13
            localTransactionTime12 = financialMessage.localTransactionTime12
            retrievalReferenceNumber37 = financialMessage.retrievalReferenceNumber37
        }
    }
}

