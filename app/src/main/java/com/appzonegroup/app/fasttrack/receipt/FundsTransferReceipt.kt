package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class FundsTransferReceipt(
    context: Context,
    private val request: FundsTransferRequest,
    private val transactionDate: String,
    override var isSuccessful: Boolean = false,
    override var isCustomerCopy: Boolean = true,
    override var isReprint: Boolean = false,
    override var reason: String? = null,
) :
    TransactionReceipt(context) {

    override val nodes: List<PrintNode>
        get() {
            val nodes = mutableListOf(
                LogoNode(),

                TextNode("Funds Transfer").apply {
                    align = Alignment.MIDDLE
                    wordFont = 35
                },

                TextNode(
                    """
Agent Code: ${context.localStorage.agent?.agentCode}
Agent Phone: ${request.agentPhoneNumber}
--------------------------
Amount NGN${request.amountInNaira}
Beneficiary: ${request.beneficiaryAccountName} ${request.beneficiaryAccountNumber.mask(4, 2)}
Transaction Date: $transactionDate
RRN: ${request.externalTransactionReference}"""
                )
            )

            nodes.addTransactionStatus()
            nodes.addAll(footerNodes(context))

            return nodes
        }
}