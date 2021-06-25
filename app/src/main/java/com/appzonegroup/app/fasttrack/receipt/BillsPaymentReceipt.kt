package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.data.response.PayBillResponse
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode
import kotlinx.serialization.json.Json
import java.time.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/28/2019.
 * Appzone Ltd
 */

class BillsPaymentReceipt(context: Context, val request: PayBillRequest) :
    TransactionReceipt(context) {

    private var additionalInformation: PayBillResponse.AdditionalInformation? = null

    override val nodes: List<PrintNode>
        get() {
            val nodes = mutableListOf(
                LogoNode(),

                TextNode(if (request.isRecharge) "Airtime Recharge" else "Bills Payment").apply {
                    align = Alignment.MIDDLE
                    wordFont = 35
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
Transaction Date: ${Instant.now().toString("dd-MM-yyyy hh:mm")}
Customer Account: ${request.customerId?.mask(4, 2)}
Customer Name: ${request.customerName}
RRN: ${request.retrievalReferenceNumber}
Transaction ID: ${request.customerDepositSlipNumber}"""
                )
            )

            additionalInformation?.customerAddress?.run {
                nodes.add(TextNode("Customer Address: $this"))
            }

            additionalInformation?.customerToken?.run {
                nodes.add(TextNode("Customer Token: $this"))
            }

            nodes.addTransactionStatus()
            nodes.addAll(footerNodes(context))
            return nodes.toList()
        }

    fun withResponse(response: PayBillResponse?): BillsPaymentReceipt {
        response ?: return this

        isSuccessful = response.isSuccessFul == true
        reason = response.responseMessage
        response.additionalInformation?.run {
            val json = Json {
                isLenient = true
                ignoreUnknownKeys = true
                allowSpecialFloatingPointValues = true
                useArrayPolymorphism = true
                encodeDefaults = true
            }
            val serializer = PayBillResponse.AdditionalInformation.serializer()
            additionalInformation = json.decodeFromString(serializer, this)
        }

        return this
    }
}