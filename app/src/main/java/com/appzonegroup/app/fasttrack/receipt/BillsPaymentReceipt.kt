package com.appzonegroup.app.fasttrack.receipt

import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.creditclub.pos.printer.Alignment
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.appzonegroup.creditclub.pos.printer.PrintJob
import com.appzonegroup.creditclub.pos.printer.TextNode
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class BillsPaymentReceipt(request: PayBillRequest) : PrintJob {
    override val nodes = listOf(
        LogoNode(),
        TextNode("Bills Payment").apply {
            align = Alignment.MIDDLE
            wordFont = 2
        },
        TextNode(
            """
Amount : ${request.amount}

Payment Item: ${request.paymentItemName}
Payment Item Code: ${request.paymentItemCode}

Biller Name: ${request.billerName}

Biller Category: ${request.billerCategoryName}

Institution Name:
Institution Code: ${request.institutionCode}
Agent Phone: ${request.agentPhoneNumber}
Transaction Date: ${Instant.now().toString("dd-MM-YYYY hh:mm")}

Customer Account: ${request.customerId?.mask(2, 2)}
Customer Name: ${request.customerName}

Slip Number: ${request.customerDepositSlipNumber}

-----------------------------

Please retain your receipt.
Thank You.

CreditClub Plus v${BuildConfig.VERSION_NAME}
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