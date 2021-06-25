package com.appzonegroup.creditclub.pos.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.R
import com.creditclub.core.data.response.BackendResponse
import com.creditclub.pos.printer.*
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/10/2019.
 * Appzone Ltd
 */
@Suppress("UNCHECKED_CAST")
abstract class TransactionReceipt(
    val context: Context,
    open var isSuccessful: Boolean = false,
    open var isCustomerCopy: Boolean = true,
    open var isReprint: Boolean = false,
    open var reason: String? = null,
) :
    PrintJob {

    open val statusMessage
        get() = (if (isSuccessful) context.getString(R.string.pos_transaction_approved)
        else context.getString(R.string.pos_transaction_declined))

    open fun MutableList<PrintNode>.addTransactionStatus() {

        add(
            TextNode(statusMessage.toUpperCase(Locale.getDefault()))
                .apply {
                    align = Alignment.MIDDLE
                    wordFont = 30
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

fun PrintJobScope.transactionStatus(
    isSuccessful: Boolean,
    statusMessage: String,
    reason: String? = null,
) {
    text(statusMessage.toUpperCase(Locale.getDefault()), align = Alignment.MIDDLE, fontSize = 30)

    if (!isSuccessful) {
        text(reason ?: "Error", align = Alignment.MIDDLE)
    }
}