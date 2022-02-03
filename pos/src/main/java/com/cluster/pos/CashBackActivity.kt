package com.cluster.pos

import com.cluster.pos.card.cardIsoMsg
import com.cluster.pos.extension.additionalAmounts54
import com.cluster.pos.models.messaging.FinancialMessage
import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType

class CashBackActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.CashBack

    private var cashBackAmount: String = "0"

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        val request = cardIsoMsg(cardData, ::FinancialMessage) {
            processingCode3 = processingCode("09")
            withParameters(parameters.parameters)
            val accountType = viewModel.accountType.value
            additionalAmounts54 = "${accountType?.code ?: "00"}00${parameters.parameters.currencyCode}D${cashBackAmount.padStart(12, '0')}"
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage("Amount") { amount ->
            viewModel.amountString.value = amount.toString()
            sessionData.amount = amount
            showAmountPage("Cash Back Amount") { cashBackAmount ->
                this.cashBackAmount = cashBackAmount.toString()
                sessionData.cashBackAmount = cashBackAmount
                confirmAmounts(sessionData) {
                    readCard()
                }
            }
        }
    }
}
