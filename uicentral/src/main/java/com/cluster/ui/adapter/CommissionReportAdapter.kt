package com.cluster.ui.adapter

import com.cluster.core.data.model.CommissionReport
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.core.util.toInstant
import com.cluster.core.util.toString
import com.cluster.ui.R
import com.cluster.ui.databinding.ItemCommissionBinding


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class CommissionReportAdapter(override var values: List<CommissionReport.Report>) :
    SimpleBindingAdapter<CommissionReport.Report, ItemCommissionBinding>(R.layout.item_commission) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val commission = values[position]

        with(holder.binding) {
            report = commission
            commissionAmountTv.text = commission.transactionAmount.toString()
//            displayMessageTv.text = commission.displayMessage
            logDate = commission.logDate.toInstant(CREDIT_CLUB_DATE_PATTERN).toString("dd EEEE MMM uuuu hh:mm:ss")
//            settlementDate = commission.settlementDate.toInstant(CREDIT_CLUB_DATE_PATTERN).timeAgo()
//            datePaid = commission.datePaid.toInstant(CREDIT_CLUB_DATE_PATTERN).timeAgo()
        }
    }
}