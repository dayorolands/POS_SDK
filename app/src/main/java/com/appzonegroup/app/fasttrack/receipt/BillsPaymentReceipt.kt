package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.logo
import com.appzonegroup.creditclub.pos.receipt.transactionStatus
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.data.response.PayBillResponse
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.core.util.toString
import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.printJob
import java.time.Instant

fun billsPaymentReceipt(
    context: Context,
    request: PayBillRequest,
    response: PayBillResponse? = null,
) = printJob {
    logo()
    text(
        if (request.isRecharge) "Airtime Recharge" else "Bills Payment",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
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

    response?.additionalInformation?.run {
        val additionalInfo = defaultJson.decodeFromString(
            deserializer = PayBillResponse.AdditionalInformation.serializer(),
            string = this,
        )

        text("Customer Address: ${additionalInfo.customerAddress}")
        text("Customer Token: ${additionalInfo.customerToken}")
    }

    transactionStatus(
        context = context,
        isSuccessful = response?.isSuccessFul == true,
        reason = response?.responseMessage,
    )
    footer(context)
}