package com.appzonegroup.app.fasttrack.receipt

import com.appzonegroup.app.fasttrack.model.WithdrawalRequest
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.appzonegroup.creditclub.pos.printer.PrintJob
import com.appzonegroup.creditclub.pos.printer.TextNode
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class WithdrawalReceipt(request: WithdrawalRequest, accountInfo: AccountInfo) : PrintJob {
    override val nodes = listOf(
        LogoNode(),
        TextNode("Withdrawal").apply {
            align = Alignment.MIDDLE
            wordFont = 2
        },
        TextNode(
            """
Amount : NGN${request.amount}

Institution Name:
Institution Code: ${request.institutionCode}
Agent Phone: ${request.agentPhoneNumber}

Customer Account: ${accountInfo.number.mask(4, 2)}
Customer Name: ${accountInfo.accountName}

Transaction Date: ${Instant.now().toString("dd-MM-YYYY hh:mm")}

-----------------------------

Please retain your receipt.
Thank You.

CreditClub POS v${BuildConfig.VERSION_NAME}
-----------------------------
            """
        ),
        TextNode("Powered by CreditClub").apply {
            align = Alignment.MIDDLE
        },
        TextNode("http://www.appzonegroup.com/products/creditclub").apply {
            align = Alignment.MIDDLE
            walkPaperAfterPrint = 20
        }
    )
}