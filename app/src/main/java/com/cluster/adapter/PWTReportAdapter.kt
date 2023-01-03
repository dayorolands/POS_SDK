package com.cluster.adapter

import com.cluster.R
import com.cluster.core.data.model.PosTransactionReport
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.ui.databinding.ItemPayWithTransferBinding

class PWTReportAdapter(override var values: List<PosTransactionReport.Report>) :
    SimpleBindingAdapter<PosTransactionReport.Report, ItemPayWithTransferBinding>(R.layout.item_pay_with_transfer){

    private var listener: OnPrintClickListener? = null

    fun setOnPrintClickListener(listener: OnPrintClickListener) {
        this.listener = listener
    }

    fun interface OnPrintClickListener {
        fun onClick(item: PosTransactionReport.Report, type: TransactionType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = values[position]

        with(holder.binding){

        }
    }
}