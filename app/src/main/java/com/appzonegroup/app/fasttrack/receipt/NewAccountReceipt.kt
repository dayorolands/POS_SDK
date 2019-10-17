package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.appzonegroup.creditclub.pos.printer.PrintNode
import com.appzonegroup.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class NewAccountReceipt(context: Context) : TransactionReceipt(context) {

    var bvn: String? = null
    var institutionCode: String = ""
    var accountNumber: String = ""
    var agentPhoneNumber: String = ""
    var uniqueReferenceID: String = ""
    var accountName: String = ""

    private val isWalletAccount get() = bvn.isNullOrEmpty()

    override val nodes: MutableList<PrintNode>
        get() {
            return mutableListOf(
                LogoNode(),

                TextNode(if (isWalletAccount) "New Wallet" else "New Account").apply {
                    align = Alignment.MIDDLE
                    wordFont = 2
                },

                TextNode(
                    """
Agent Code: ${context.localStorage.agent?.agentCode}
Agent Phone: ${context.localStorage.agent?.phoneNumber}
--------------------------

Account Name: $accountName
Account Number: ${accountNumber.mask(4, 2)}
Account Type: ${if (isWalletAccount) "Wallet Account" else "Savings Account"}

Creation Date: ${Instant.now().toString("dd-MM-YYYY hh:mm")}
RRN: $uniqueReferenceID"""
                )
            ).apply { addTransactionStatus(); addAll(polarisFooterNodes) }
        }
}