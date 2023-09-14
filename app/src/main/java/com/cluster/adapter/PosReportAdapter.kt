package com.cluster.adapter

import android.view.View
import com.cluster.core.data.model.PosTransactionReport
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.pos.Platform
import com.cluster.core.util.CREDIT_CLUB_DATE_PATTERN
import com.cluster.core.util.timeAgo
import com.cluster.core.util.toInstant
import com.cluster.ui.R
import com.cluster.ui.databinding.ItemPosCashoutBinding

class PosReportAdapter(override var values: List<PosTransactionReport.Report>) :
    SimpleBindingAdapter<PosTransactionReport.Report, ItemPosCashoutBinding>(R.layout.item_pos_cashout) {

    private var listener: OnPrintClickListener? = null

    fun setOnPrintClickListener(listener: OnPrintClickListener) {
        this.listener = listener
    }

    fun interface OnPrintClickListener {
        fun onClick(item: PosTransactionReport.Report, type: TransactionType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = values[position]

        with(holder.binding) {
            amountTv.text = transaction.transactionAmount.toString()
            customerNameTv.text = transaction.transactionReference
            timeOccurredTv.text =
                transaction.dateLogged.toInstant(CREDIT_CLUB_DATE_PATTERN).timeAgo()
            toTv.text = null
            if (transaction.settlementDate == null) {
                dateSettledTv.text = "Unsettled"
                dateSettledTv.setTextColor(
                    dateSettledTv.context.resources.getColor(R.color.colorAccent)
                )
            } else {
                dateSettledTv.text =
                    "Settled ${transaction.settlementDate?.toInstant(CREDIT_CLUB_DATE_PATTERN)?.timeAgo()} with NGN${transaction.amountCreditedToAgent}"
                dateSettledTv.setTextColor(
                    dateSettledTv.context.resources.getColor(R.color.green)
                )
            }
            if (Platform.hasPrinter) {
                printReceiptButton.visibility = View.VISIBLE
                printReceiptButton.setOnClickListener{
                    listener?.onClick(transaction, TransactionType.POSCashOut)}
            } else {
                printReceiptButton.visibility = View.GONE
            }
        }
    }
}