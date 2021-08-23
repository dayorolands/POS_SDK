package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.data.request.DepositRequest
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.core.util.toString
import java.time.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class DepositReceipt(
    context: Context,
    private val request: DepositRequest,
    private val accountInfo: AccountInfo,
    private val transactionDate: String,
    override var isCustomerCopy: Boolean = true,
    override var isReprint: Boolean = false,
    override var isSuccessful: Boolean = false,
    override var reason: String? = null,
) :
    TransactionReceipt(context, isSuccessful, isCustomerCopy, isReprint, reason) {

    override val nodes: MutableList<PrintNode>
        get() {
            return mutableListOf(
                LogoNode(),

                TextNode("Deposit").apply {
                    align = Alignment.MIDDLE
                    wordFont = 35
                },

                TextNode(
                    """
Agent Code: ${context.localStorage.agent?.agentCode}
Agent Phone: ${request.agentPhoneNumber}
--------------------------
Amount ${request.amount?.toDouble()?.toCurrencyFormat()}

Customer Account: ${accountInfo.number.mask(4, 2)}
Customer Name: ${accountInfo.accountName}

Transaction Date: $transactionDate}
RRN: ${request.retrievalReferenceNumber}"""
                )
            ).apply { addTransactionStatus(); addAll(footerNodes(context)) }
        }
}