package com.cluster.pos.receipt

import android.content.Context
import com.cluster.pos.R
import com.cluster.core.data.response.BackendResponse
import com.cluster.pos.printer.*
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
            TextNode(statusMessage.uppercase(Locale.getDefault()))
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
    context: Context,
    isSuccessful: Boolean,
    statusMessage: String = (if (isSuccessful) {
        context.getString(R.string.pos_transaction_approved)
    } else {
        context.getString(R.string.pos_transaction_declined)
    }),
    reason: String? = null,
) {
    text(
        text = statusMessage.uppercase(Locale.getDefault()),
        align = Alignment.MIDDLE,
        fontSize = 30,
    )

    if (!isSuccessful) {
        text(reason ?: "Error", align = Alignment.MIDDLE)
    }
}