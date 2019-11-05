package com.creditclub.ui.adapter

import com.creditclub.core.data.model.CommissionReport
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.CREDIT_CLUB_DATE_PATTERN
import com.creditclub.core.util.timeAgo
import com.creditclub.core.util.toInstant
import com.creditclub.core.util.toString
import com.creditclub.ui.R
import com.creditclub.ui.databinding.ItemCommissionBinding


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