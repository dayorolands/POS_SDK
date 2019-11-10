package com.appzonegroup.creditclub.pos.printer

import android.content.Context
import com.appzonegroup.creditclub.pos.models.FinancialTransaction
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.util.localStorage


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/20/2019.
 * Appzone Ltd
 */
class Receipt(val context: Context, val transaction: FinancialTransaction) : PrintJob {
    var isCustomerCopy = true
    var isReprint = false

    var merchantDetails = transaction.isoMsg.cardAcceptorNameLocation43
    var merchantId = transaction.isoMsg.cardAcceptorIdCode42
    var terminalId = transaction.isoMsg.terminalId41
    var cardExp = transaction.isoMsg.cardExpirationDate14?.run {
        "${substring(2, 4)}/${substring(0, 2)}"
    }
    var stan = transaction.isoMsg.stan11
    var rrn = transaction.isoMsg.retrievalReferenceNumber37
    var cardType = transaction.cardType
    var aid = ""
    private val successful by lazy { transaction.isoMsg.responseCode39 == "00" }
    private val cardHolder = transaction.cardHolder
    val amount by lazy {
        if (transaction.type == "BALANCE INQUIRY") CurrencyFormatter.format(transaction.isoMsg.additionalAmount)
        else CurrencyFormatter.format(transaction.isoMsg.transactionAmount4)
    }

    override val nodes: List<PrintNode>
        get() {
            val nodes = arrayListOf<PrintNode>(LogoNode())

            if (isReprint) nodes.add(TextNode("***REPRINT***").apply {
                align = Alignment.MIDDLE
            })

            nodes.add(TextNode(if (isCustomerCopy) "***CUSTOMER COPY***" else "***MERCHANT COPY***").apply {
                align = Alignment.MIDDLE
            })

            nodes.add(
                TextNode(
                    """$merchantDetails
Merchant Id: ${merchantId}
Agent Name: ${context.localStorage.agent?.agentName}
Agent Code: ${context.localStorage.agent?.agentCode}
TID: $terminalId

${transaction.prettyTime}

$cardType
Card: ${transaction.pan}
STAN: $stan
PAN Seq No: 01
Cardholder: $cardHolder
EXP:$cardExp
Acquirer:AppZone
PTSP:3GEE PAY
Verification Mode: PIN"""
                )
            )

            nodes.add(TextNode(transaction.type).apply {
                align = Alignment.MIDDLE
                wordFont = 25
            })

            nodes.add(
                TextNode(
                    """AMOUNT: $amount
RRN: $rrn"""
                )
            )

            nodes.add(TextNode(if (successful) "TRANSACTION APPROVED" else "TRANSACTION DECLINED").apply {
                align = Alignment.MIDDLE
                wordFont = 25
            })

            if (!successful) nodes.add(TextNode(transaction.isoMsg.responseMessage).apply {
                align = Alignment.MIDDLE
            })

            nodes.addAll(footerNodes)

            return nodes
        }
}