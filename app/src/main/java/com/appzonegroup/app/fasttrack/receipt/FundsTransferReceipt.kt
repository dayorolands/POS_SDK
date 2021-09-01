package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.logo
import com.appzonegroup.creditclub.pos.receipt.transactionStatus
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.mask
import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.printJob

fun fundsTransferReceipt(
    context: Context,
    request: FundsTransferRequest,
    transactionDate: String,
    isSuccessful: Boolean = false,
    reason: String? = null,
) = printJob {
    logo()
    text(
        text = "Funds Transfer",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
Agent Code: ${context.localStorage.agent?.agentCode}
Agent Phone: ${request.agentPhoneNumber}
--------------------------
Amount NGN${request.amountInNaira}
Beneficiary: ${request.beneficiaryAccountName} ${request.beneficiaryAccountNumber.mask(4, 2)}
Transaction Date: $transactionDate
RRN: ${request.externalTransactionReference}"""
    )
    transactionStatus(
        context = context,
        isSuccessful = isSuccessful,
        reason = reason,
    )
    footer(context)
}