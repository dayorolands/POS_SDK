package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.DataBindingUtil
import com.appzonegroup.app.fasttrack.databinding.TransactionStatusFragmentBinding
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.activity.ReceiptActivity
import com.creditclub.core.data.model.PendingTransaction
import com.creditclub.core.data.response.BackendResponse
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.*
import com.creditclub.pos.printer.ParcelablePrintJob
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrintJob
import com.creditclub.pos.printer.PrinterStatus
import io.objectbox.Box
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

internal fun CreditClubActivity.renderTransactionStatusPage(
    title: String,
    amount: Long,
    isSuccessful: Boolean,
    responseMessage: String?,
    receipt: PrintJob?,
) {
    val binding = DataBindingUtil.setContentView<TransactionStatusFragmentBinding>(
        this,
        R.layout.transaction_status_fragment
    )
    binding.title = title

    if (isSuccessful) {
        binding.message.text = getString(R.string.transaction_successful)
        binding.transactionStatusIcon.setImageResource(R.drawable.ic_sentiment_satisfied)
        ImageViewCompat.setImageTintList(
            binding.transactionStatusIcon, ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorPrimary)
            )
        )
        binding.message.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
    } else {
        binding.message.text = responseMessage
        binding.transactionStatusIcon.setImageResource(R.drawable.ic_sentiment_very_dissatisfied)
        ImageViewCompat.setImageTintList(
            binding.transactionStatusIcon, ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.app_orange)
            )
        )
        binding.message.setTextColor(ContextCompat.getColor(this, R.color.app_orange))
    }

    binding.amountText.text = amount.toCurrencyFormat()

    if (Platform.hasPrinter && receipt != null) {
        val printer = get<PosPrinter> { parametersOf(this, dialogProvider) }
        binding.printReceiptButton.visibility = View.VISIBLE
        binding.printReceiptButton.setOnClickListener {
            printer.printAsync(receipt) { printerStatus ->
                if (printerStatus != PrinterStatus.READY) dialogProvider.showError(printerStatus.message)
            }
        }
    }

    val closeListener = View.OnClickListener { finish() }
    binding.goBack.setOnClickListener(closeListener)
}

fun CreditClubActivity.startReceiptActivity(receipt: ParcelablePrintJob) {
    val intent = Intent(this, ReceiptActivity::class.java).apply {
        putExtra("receipt", receipt)
    }
    startActivity(intent)
}

suspend inline fun <T : BackendResponse> executeTransaction(
    crossinline fetcher: suspend () -> T?,
    crossinline reFetcher: suspend () -> T?,
    pendingTransaction: PendingTransaction,
    dialogProvider: DialogProvider,
    pendingTransactionsBox: Box<PendingTransaction>,
): SafeRunResult<T?> = coroutineScope {
    var result: SafeRunResult<T?>
    var tryCount = 0
    var pendingTransactionId: Long? = null

    transactionLoop@ while (true) {
        result = if (tryCount == 0) {
            dialogProvider.showProgressBar("Processing")
            safeRunIO(block = fetcher)
        } else {
            dialogProvider.showProgressBar("Verifying Transaction Status\nRetrying....(count = $tryCount)") {
                onClose {
                    dialogProvider.hideProgressBar()
                    this@coroutineScope.cancel()
                }
            }
            safeRunIO(block = reFetcher)
        }
        tryCount++
        val response = result.data

        // Store pending and unconfirmed transaction for requery
        if ((response == null || response.isPending()) && pendingTransactionId == null) {
            pendingTransactionId = pendingTransactionsBox.put(pendingTransaction)
        }

        if (result.isFailure) {
            val error = result.error!!
            if (error.isTimeout() || error.isInternalServerError()) {
                tryCount++
                continue@transactionLoop
            }

            break@transactionLoop
        }

        if (response != null) {
            when {
                response.isSuccess() -> {
                    if (pendingTransactionId != null) {
                        // delete pending transaction once confirmed successful
                        pendingTransactionsBox.remove(pendingTransactionId)
                    }
                    break@transactionLoop
                }
                response.isPendingOnBank() -> {
                    dialogProvider.hideProgressBar()
                    val retry = dialogProvider.getConfirmation(
                        title = "Transaction pending",
                        subtitle = "Check Status?"
                    )
                    if (retry) {
                        continue@transactionLoop
                    } else {
                        break@transactionLoop
                    }
                }
                response.isPendingOnMiddleware() -> {
                    continue@transactionLoop
                }
                response.isFailure() -> {
                    if (pendingTransactionId != null) {
                        // delete pending transaction once confirmed failed
                        pendingTransactionsBox.remove(pendingTransactionId)
                    }
                    break@transactionLoop
                }
            }
        }
    }
    dialogProvider.hideProgressBar()

    return@coroutineScope result
}