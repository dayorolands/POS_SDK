package com.cluster.pos.printer

import android.content.Context
import com.cluster.core.data.api.AppConfig
import com.cluster.core.util.format
import com.cluster.core.util.localStorage
import com.cluster.pos.R
import com.cluster.pos.card.isoResponseMessage
import com.cluster.pos.extension.*
import com.cluster.pos.models.FinancialTransaction
import com.cluster.pos.models.PosTransaction
import com.cluster.pos.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jpos.iso.ISOMsg
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class Receipt(val context: Context, val transaction: FinancialTransaction) :
    PrintJob, KoinComponent {
    private val backendConfig by inject<AppConfig>()
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

            if (isReprint) nodes.add(
                TextNode("***REPRINT***").apply {
                    align = Alignment.MIDDLE
                })

            nodes.add(
                TextNode(if (isCustomerCopy) "***CUSTOMER COPY***" else "***MERCHANT COPY***")
                    .apply {
                        align = Alignment.MIDDLE
                    })

            nodes.add(
                TextNode(
                    """$merchantDetails
Merchant Id: $merchantId
Agent Name: ${context.localStorage.agent?.agentName}
Agent Code: ${context.localStorage.agent?.agentCode}
TID: $terminalId

${transaction.createdAt.format("dd/MM/yyyy hh:mm", "+0100")}

$cardType
Card: ${transaction.pan}
STAN: $stan
PAN Seq No: 01
Cardholder: $cardHolder
EXP:$cardExp
Acquirer: ${context.getString(R.string.pos_acquirer)}
PTSP:${context.getString(R.string.ptsp_name)}
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

            nodes.add(
                TextNode(if (successful) "TRANSACTION APPROVED" else "TRANSACTION DECLINED")
                    .apply {
                        align = Alignment.MIDDLE
                        wordFont = 25
                    })

            if (!successful) nodes.add(
                TextNode(
                    transaction.isoMsg.responseMessage
                ).apply {
                    align = Alignment.MIDDLE
                })

            nodes.add(
                TextNode("-----------------------------").apply {
                    align = Alignment.MIDDLE
                    wordFont = 10
                })

            nodes.add(TextNode(
                "${context.getString(R.string.app_name)} v${backendConfig.versionName}. Powered by ${
                    context.getString(
                        R.string.institution_name
                    )
                }"
            ).apply {
                align = Alignment.MIDDLE
                wordFont = 15
            })

            nodes.add(
                TextNode(
                    context.getString(R.string.institution_website),
                    align = Alignment.MIDDLE,
                    walkPaperAfterPrint = 15,
                    wordFont = 15,
                )
            )

            return nodes
        }
}

fun posReceipt(
    posTransaction: PosTransaction,
    isCustomerCopy: Boolean = false,
    isReprint: Boolean = false,
) = printJob {
    logo()
    if (isReprint) {
        text(
            text = "***REPRINT***",
            align = Alignment.MIDDLE,
            isBold = true,
        )
    }
    text(
        text = if (isCustomerCopy) "***CUSTOMER COPY***" else "***MERCHANT COPY***",
        align = Alignment.MIDDLE,
        isBold = true,
    )
    text(
        """${posTransaction.merchantDetails}
            |Merchant Id: ${posTransaction.merchantId}
            |Agent Name: ${posTransaction.agentName}
            |Agent Code: ${posTransaction.agentCode}
            |TID: ${posTransaction.terminalId}
            |
            |${posTransaction.dateTime?.format("dd/MM/yyyy hh:mm", "+0100")}
            |
            |${posTransaction.cardType}
            |Card: ${posTransaction.pan}
            |STAN: ${posTransaction.stan}
            |PAN Seq No: 01
            |Cardholder: ${posTransaction.cardHolder}
            |EXP:${posTransaction.expiryDate}
            |Acquirer: ${posTransaction.bankName}
            |PTSP:${posTransaction.ptsp}
            |Verification Mode: PIN""".trimMargin(),
        isBold = true,
        printGray = 5
    )
    text(
        text = posTransaction.transactionType!!,
        align = Alignment.MIDDLE,
        fontSize = 35
    )
    text(
        text = """AMOUNT: ${posTransaction.amount}
            |RRN: ${posTransaction.retrievalReferenceNumber}""".trimMargin(),
        isBold = true
    )

    val isSuccessful = posTransaction.responseCode == "00"
    val message = if (isSuccessful) "TRANSACTION APPROVED" else "TRANSACTION DECLINED"
    text(
        text = message,
        align = Alignment.MIDDLE,
        fontSize = 35,
        isBold = true
    )
    if (!isSuccessful) {
        text(
            text = isoResponseMessage(posTransaction.responseCode),
            align = Alignment.MIDDLE,
            isBold = true
        )
    }
    text(
        text = "-------------------------------------",
        align = Alignment.MIDDLE,
        fontSize = 15,
        isBold = true
    )
    text(
        text = "${posTransaction.appName}. Powered by ${posTransaction.bankName}",
        align = Alignment.MIDDLE,
        fontSize = 15,
        isBold = true
    )
    text(
        text = posTransaction.website!!,
        align = Alignment.MIDDLE,
        fontSize = 15,
        isBold = true
    )

}

inline val ISOMsg.additionalAmount: String
    get() = try {
        additionalAmounts54?.substring(8, 20) ?: "0"
    } catch (ex: Exception) {
        "0"
    }