package com.appzonegroup.app.fasttrack

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.DataBindingUtil
import com.appzonegroup.app.fasttrack.databinding.TransactionStatusFragmentBinding
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.toCurrencyFormat
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrintJob
import org.koin.android.ext.android.get

internal fun CreditClubActivity.renderTransactionStatusPage(
    title: String,
    amount: Long,
    isSuccessful: Boolean,
    responseMessage: String?,
    receipt: PrintJob?
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
        val printer = get<PosPrinter>()
        binding.printReceiptButton.visibility = View.VISIBLE
        binding.printReceiptButton.setOnClickListener {
            printer.printAsync(receipt)
        }
    }

    val closeListener = View.OnClickListener { finish() }
    binding.goBack.setOnClickListener(closeListener)
}