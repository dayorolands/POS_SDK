package com.cluster.pos.printer

import android.content.Context
import com.cluster.pos.R
import com.cluster.pos.extension.additionalAmounts54
import com.cluster.pos.extension.cardAcceptorIdCode42
import com.cluster.pos.extension.cardAcceptorNameLocation43
import com.cluster.pos.extension.cardExpirationDate14
import com.cluster.pos.models.FinancialTransaction
import com.cluster.pos.util.CurrencyFormatter
import com.cluster.core.data.api.AppConfig
import com.cluster.core.util.format
import com.cluster.core.util.localStorage
import org.jpos.iso.ISOMsg
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/20/2019.
 * Appzone Ltd
 */
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
                    wordFont = 15
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
                    walkPaperAfterPrint = 10,
                    wordFont = 15,
                )
            )

            return nodes
        }
}

inline val ISOMsg.additionalAmount: String
    get() = try {
        additionalAmounts54?.substring(8, 20) ?: "0"
    } catch (ex: Exception) {
        "0"
    }