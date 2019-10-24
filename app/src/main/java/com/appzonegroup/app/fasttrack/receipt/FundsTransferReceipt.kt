package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.appzonegroup.creditclub.pos.printer.PrintNode
import com.appzonegroup.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class FundsTransferReceipt(context: Context, val request: FundsTransferRequest) :
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

Beneficiary Name: ${request.beneficiaryAccountName}
Beneficiary Account Number: ${request.beneficiaryAccountNumber.mask(4, 2)}

Transaction Date: ${Instant.now().toString("dd-MM-YYYY hh:mm")}
RRN: ${request.externalTransactionReference}"""
                )
            )

            nodes.addTransactionStatus()
            nodes.addAll(footerNodes(context))

            return nodes
        }
}