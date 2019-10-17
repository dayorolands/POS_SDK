package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.*
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.data.response.PayBillResponse
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class BillsPaymentReceipt(context: Context, val request: PayBillRequest) :
    TransactionReceipt(context) {

    override val nodes: List<PrintNode>
        get() {
            return mutableListOf(
                LogoNode(),

                TextNode(if (request.isRecharge) "Airtime Recharge" else "Bills Payment").apply {
                    align = Alignment.MIDDLE
                    wordFont = 2
                },

                TextNode(
                    """
Agent Code: ${context.localStorage.agent?.agentCode}
Agent Phone: ${request.agentPhoneNumber}
--------------------------
Amount NGN${request.amount}

Payment Item: ${request.paymentItemName}
Payment Item Code: ${request.paymentItemCode}

Biller Name: ${request.billerName}
Biller Category: ${request.billerCategoryName}

Transaction Date: ${Instant.now().toString("dd-MM-YYYY hh:mm")}

Customer Account: ${request.customerId?.mask(4, 2)}
Customer Name: ${request.customerName}

RRN: ${request.retrievalReferenceNumber}
Transaction ID: ${request.customerDepositSlipNumber}"""
                )
            ).apply { addTransactionStatus(); addAll(footerNodes) }.toList()
        }

    fun withResponse(response: PayBillResponse?): BillsPaymentReceipt {
        isSuccessful = response?.isSuccessFul == true
        reason = response?.responseMessage

        return this
    }
}