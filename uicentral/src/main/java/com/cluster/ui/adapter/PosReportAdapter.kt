package com.cluster.ui.adapter

import com.cluster.core.data.model.PosTransactionReport
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.core.util.timeAgo
import com.cluster.core.util.toInstant
import com.cluster.ui.R
import com.cluster.ui.databinding.ItemPosCashoutBinding


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class PosReportAdapter(override var values: List<PosTransactionReport.Report>) :
    SimpleBindingAdapter<PosTransactionReport.Report, ItemPosCashoutBinding>(R.layout.item_pos_cashout) {

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
        }
    }
}