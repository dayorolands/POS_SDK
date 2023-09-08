package com.cluster.pos

import com.cluster.pos.card.CardData
import com.cluster.pos.card.TransactionType
import com.cluster.pos.card.applyCardData
import com.cluster.pos.extension.additionalAmounts54
import com.cluster.pos.extension.processingCode3
import com.cluster.pos.util.ISO87Packager
import org.jpos.iso.ISOMsg

class CashBackActivity : CardTransactionActivity() {
    override var transactionType = TransactionType.CashBack

    private var cashBackAmount: String = "0"

    override fun onPosReady() {
        requestCard()
    }

    override fun onReadCard(cardData: CardData) {
        val accountTypeCode = viewModel.accountType.value?.code ?: "00"
        val currencyCode = posParameter.managementData.currencyCode
        val paddedCashBack = cashBackAmount.padStart(12, '0')

        val request = ISOMsg().apply {
            packager = ISO87Packager()
            mti = "0200"
            processingCode3 = processingCode("09")
            applyCardData(cardData)
            additionalAmounts54 = "${accountTypeCode}00${currencyCode}D${paddedCashBack}"
        }

        makeRequest(request)
    }

    override fun onSelectAccountType() {
        showAmountPage("Amount") { amount ->
            viewModel.amountString.value = amount.toString()
            sessionData.amount = amount
            showAmountPage("Cash Back Amount") { cashBackAmount ->
                this.cashBackAmount = cashBackAmount.toString()
                viewModel.cashbackString.value = cashBackAmount.toString()
                sessionData.cashBackAmount = cashBackAmount
                confirmAmounts(sessionData) {
                    readCard()
                }
            }
        }
    }
}

