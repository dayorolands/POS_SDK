package com.cluster.adapter

import com.cluster.R
import com.cluster.core.data.model.PWTTransactionReportResponse
import com.cluster.core.data.model.PosTransactionReport
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.core.util.CREDIT_CLUB_DATE_PATTERN
import com.cluster.core.util.timeAgo
import com.cluster.core.util.toCurrencyFormat
import com.cluster.core.util.toInstant
import com.cluster.ui.databinding.ItemPayWithTransferBinding

class PWTReportAdapter(override var values: List<PWTTransactionReportResponse.PayWithTransferReport>) :
    SimpleBindingAdapter<PWTTransactionReportResponse.PayWithTransferReport, ItemPayWithTransferBinding>(R.layout.item_pay_with_transfer){

    private var listener: OnPrintClickListener? = null

    fun setOnPrintClickListener(listener: OnPrintClickListener) {
        this.listener = listener
    }

    fun interface OnPrintClickListener {
        fun onClick(item: PWTTransactionReportResponse.PayWithTransferReport)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = values[position]

        with(holder.binding){
            receivedAmountTv.text = transaction.amountReceived?.toCurrencyFormat()
            expectedAmountTv.text = transaction.expectedAmount?.toCurrencyFormat()
            customerNameTv.text = transaction.customerName
            virtualAccountTv.text = transaction.virtualAccountNumber
            dateTv.text = transaction.date?.replace("T", " ")
            printReceiptButton.setOnClickListener{
                listener?.onClick(transaction)
            }
        }
    }
}