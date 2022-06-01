package com.cluster.pos

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.DataBindingUtil
import com.cluster.core.util.format
import com.cluster.pos.card.AccountType
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.TransactionType
import com.cluster.pos.card.isoResponseMessage
import com.cluster.pos.databinding.*
import com.cluster.pos.extension.transactionAmount4
import com.cluster.pos.models.FinancialTransaction
import com.cluster.pos.models.PosTransaction
import com.cluster.pos.printer.PrinterStatus
import com.cluster.pos.printer.posReceipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal inline fun ComponentActivity.showSelectAccountScreen(crossinline block: (AccountType) -> Unit) {
    val binding = DataBindingUtil.setContentView<PageSelectAccountTypeBinding>(
        this,
        R.layout.page_select_account_type
    )
    binding.lifecycleOwner = this

    binding.savingsRadioButton.button.setOnClickListener { block(AccountType.Savings) }
    binding.defaultRadioButton.button.setOnClickListener { block(AccountType.Default) }
    binding.currentRadioButton.button.setOnClickListener { block(AccountType.Current) }
    binding.creditRadioButton.button.setOnClickListener { block(AccountType.Credit) }

    binding.header.goBack.setOnClickListener { onBackPressed() }
}

internal inline fun CardTransactionActivity.showAmountPage(
    title: String = getString(R.string.amount),
    initialValue: Long = 0L,
    crossinline block: (amount: Long) -> Unit
) {
    restartTimer()
    val binding: PageInputAmountBinding =
        DataBindingUtil.setContentView(this, R.layout.page_input_amount)
    binding.lifecycleOwner = this
    binding.header.subtitle = title

    this.title = "Amount"
    var amountString = "$initialValue"
    binding.amountTv.text = ((amountString.toDoubleOrNull() ?: 0.0) / 100).format()

    binding.cancelButton.setOnClickListener {
        val amount = amountString.toLongOrNull() ?: 0L
        if (amount > 0) finish()
        else renderTransactionFailure("Please Remove Card", "")
    }
    binding.selectAmountButton.setOnClickListener {
        val longAmount = amountString.toLongOrNull() ?: 0L
        if (longAmount == 0L) {
            Toast.makeText(this, "Amount cannot be zero", Toast.LENGTH_LONG).show()
            return@setOnClickListener
        }

        block(longAmount)
    }
    val presetNumberClickListener = View.OnClickListener { v ->
        restartTimer()
        amountString = when (v?.id) {
            R.id.number2000 -> "200000"
            R.id.number5000 -> "500000"
            R.id.number10000 -> "1000000"
            else -> "0"
        }
        binding.amountTv.text = ((amountString.toDoubleOrNull() ?: 0.0) / 100).format()
    }
    val numberClickListener: (Number) -> View.OnClickListener = { num ->
        View.OnClickListener {
            restartTimer()
            if (amountString.length > 7) return@OnClickListener

            amountString = "$amountString$num".toLong().toString()
            binding.amountTv.text = ((amountString.toDoubleOrNull() ?: 0.0) / 100).format()
        }
    }
    binding.number2000.setOnClickListener(presetNumberClickListener)
    binding.number5000.setOnClickListener(presetNumberClickListener)
    binding.number10000.setOnClickListener(presetNumberClickListener)

    val numberPad = binding.numberPad
    numberPad.number0.setOnClickListener(numberClickListener(0))
    numberPad.number1.setOnClickListener(numberClickListener(1))
    numberPad.number2.setOnClickListener(numberClickListener(2))
    numberPad.number3.setOnClickListener(numberClickListener(3))
    numberPad.number4.setOnClickListener(numberClickListener(4))
    numberPad.number5.setOnClickListener(numberClickListener(5))
    numberPad.number6.setOnClickListener(numberClickListener(6))
    numberPad.number7.setOnClickListener(numberClickListener(7))
    numberPad.number8.setOnClickListener(numberClickListener(8))
    numberPad.number9.setOnClickListener(numberClickListener(9))

    numberPad.clearBtn.setOnClickListener {
        amountString = "0"
        binding.amountTv.text = ((amountString.toDoubleOrNull() ?: 0.0) / 100).format()
    }
}

@SuppressLint("SetTextI18n")
internal inline fun CardTransactionActivity.confirmAmounts(
    sessionData: PosManager.SessionData,
    crossinline block: () -> Unit
) {
    block()
}

internal fun CardTransactionActivity.renderTransactionFailure(
    message: String = "",
    subMessage: String = "Please remove card"
) {
    val binding = DataBindingUtil.setContentView<PageTransactionErrorBinding>(
        this,
        R.layout.page_transaction_error
    )
    binding.lifecycleOwner = this
    val cardReaderEvent = viewModel.cardReaderEvent.value
    binding.message.text = message
    binding.subMessage.text = if (cardReaderEvent == CardReaderEvent.CHIP) subMessage else ""

    onTransactionDidFinish()

    val closeListener = View.OnClickListener { finish() }
    binding.closeBtn.setOnClickListener(closeListener)
    binding.goBack.setOnClickListener(closeListener)
}

internal fun CardTransactionActivity.showTransactionStatusPage(posTransaction: PosTransaction) {
    onTransactionDidFinish()
    val binding = DataBindingUtil.setContentView<PageVerifyCashoutBinding>(
        this,
        R.layout.page_verify_cashout
    )
    binding.lifecycleOwner = this

    if (posTransaction.responseCode == "00") {
        binding.message.text = getString(R.string.transaction_successful)
        binding.transactionStatusIcon.setImageResource(R.drawable.ic_sentiment_satisfied)
        ImageViewCompat.setImageTintList(
            binding.transactionStatusIcon, ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorPrimary)
            )
        )
        binding.message.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
    } else if (posTransaction.responseCode == "24") {
        binding.message.text = getString(R.string.transaction_pending)
        binding.transactionStatusIcon.setImageResource(R.drawable.ic_sentiment_very_dissatisfied)
        ImageViewCompat.setImageTintList(
            binding.transactionStatusIcon, ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorPrimary)
            )
        )
        binding.message.setTextColor(ContextCompat.getColor(this, R.color.app_orange))
    } else {
        binding.message.text = isoResponseMessage(posTransaction.responseCode)
        binding.transactionStatusIcon.setImageResource(R.drawable.ic_sentiment_very_dissatisfied)
        ImageViewCompat.setImageTintList(
            binding.transactionStatusIcon, ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.app_orange)
            )
        )
        binding.message.setTextColor(ContextCompat.getColor(this, R.color.app_orange))
    }

    binding.amountText.text = posTransaction.amount
    if (transactionType == TransactionType.Balance) {
        binding.printMerchantCopy.visibility = View.GONE
    }

    binding.printMerchantCopy.setOnClickListener {
        val receipt = posReceipt(
            posTransaction = posTransaction,
            isCustomerCopy = false,
        )
        printer.printAsync(receipt)
    }

    binding.printCustomerCopy.setOnClickListener {
        val receipt = posReceipt(
            posTransaction = posTransaction,
            isCustomerCopy = true,
        )
        printer.printAsync(receipt)
    }

    val closeListener = View.OnClickListener { finish() }
    binding.goBack.setOnClickListener(closeListener)
}

internal fun CardTransactionActivity.showReferencePage(dialogTitle: String = "Enter RRN") {
    showReferencePage(dialogTitle) { transaction ->
        sessionData.amount = transaction.isoMsg.transactionAmount4?.toLong() ?: 0
        viewModel.amountString.value = sessionData.amount.toString()
        previousMessage = transaction.isoMsg
        requestCard()
    }
}

internal inline fun CardTransactionActivity.showReferencePage(
    title: String = "Enter RRN",
    crossinline block: (FinancialTransaction) -> Unit
) {
    mainScope.launch {
        if (Platform.hasPrinter) {
            val printerStatus = withContext(Dispatchers.IO) { printer.check() }
            if (printerStatus != PrinterStatus.READY) {
                dialogProvider.showErrorAndWait(printerStatus.message)
                finish()
                return@launch
            }
        }

        val binding = DataBindingUtil.setContentView<PageInputRrnBinding>(
            this@showReferencePage,
            R.layout.page_input_rrn
        )
        binding.lifecycleOwner = this@showReferencePage
        binding.header.subtitle = title
        binding.confirmRrnButton.setOnClickListener {
            val rrn = binding.rrnInput.text.toString().trim { it <= ' ' }
            if (rrn.length != 12) {
                return@setOnClickListener dialogProvider.showError("RRN must be 12 digits")
            }
            mainScope.launch {
                val trn = withContext(Dispatchers.IO) {
                    posDatabase.financialTransactionDao().findByReference(rrn)
                }
                if (trn == null) {
                    dialogProvider.showError("Unable to locate record") {
                        finish()
                    }
                    return@launch
                }
                block(trn)
            }
        }
        binding.header.goBack.setOnClickListener { finish() }
        binding.cancelButton.setOnClickListener { finish() }
    }
}