package com.creditclub.ui.adapter

import com.creditclub.core.data.model.PosTransactionReport
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.CREDIT_CLUB_DATE_PATTERN
import com.creditclub.core.util.timeAgo
import com.creditclub.core.util.toInstant
import com.creditclub.ui.R
import com.creditclub.ui.databinding.ItemPosCashoutBinding


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
            customerNameTv.text = transaction.customerAccountNumber
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
                    "Settled ${transaction.settlementDate?.toInstant(CREDIT_CLUB_DATE_PATTERN)?.timeAgo()}"
                dateSettledTv.setTextColor(
                    dateSettledTv.context.resources.getColor(R.color.green)
                )
            }
        }
    }
}