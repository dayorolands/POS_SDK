package com.appzonegroup.creditclub.pos.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.R
import com.creditclub.pos.printer.Alignment
import com.creditclub.pos.printer.PrintJob
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode
import com.creditclub.core.data.response.BackendResponse
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/10/2019.
 * Appzone Ltd
 */
abstract class TransactionReceipt(val context: Context) :
    PrintJob {

    var isSuccessful = false
    var isCustomerCopy = true
    var isReprint = false
    var reason: String? = null

    open val statusMessage
        get() = (if (isSuccessful) context.getString(R.string.pos_transaction_approved)
        else context.getString(R.string.pos_transaction_declined))

    open fun MutableList<PrintNode>.addTransactionStatus() {

        add(
            TextNode(statusMessage.toUpperCase(Locale.getDefault()))
                .apply {
            align = Alignment.MIDDLE
            wordFont = 35
        })

        if (!isSuccessful) {
            add(TextNode(reason ?: "Error").apply {
                align = Alignment.MIDDLE
            })
        }
    }

    open fun <T : TransactionReceipt> withResponse(response: BackendResponse?): T {
        isSuccessful = response?.isSuccessful == true
        reason = response?.responseMessage

        return this as T
    }
}