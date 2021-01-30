package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.data.request.WithdrawalRequest
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import java.time.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class WithdrawalReceipt(
    context: Context,
    val request: WithdrawalRequest,
    val accountInfo: AccountInfo
) :
    TransactionReceipt(context) {

    override val nodes: MutableList<PrintNode>
        get() {
            return mutableListOf(
                LogoNode(),
                TextNode("Withdrawal").apply {
                    align = Alignment.MIDDLE
                    wordFont = 35
                },
                TextNode(
                    """
Agent Code: ${context.localStorage.agent?.agentCode}
Agent Phone: ${request.agentPhoneNumber}
--------------------------
Amount : NGN${request.amount}

Customer Account: ${accountInfo.number.mask(4, 2)}
Customer Name: ${accountInfo.accountName}

Transaction Date: ${Instant.now().toString("dd-MM-yyyy hh:mm")}"""
                )
            ).apply { addTransactionStatus(); addAll(footerNodes(context)) }
        }
}