package com.creditclub.ui.adapter

import android.content.Context
import com.creditclub.core.data.model.MiniStatementTransaction
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.CREDIT_CLUB_DATE_PATTERN
import com.creditclub.core.util.toInstant
import com.creditclub.core.util.toString
import com.creditclub.ui.R
import com.creditclub.ui.databinding.ItemMiniStatementBinding

class MiniStatementAdapter(
    val context: Context,
    override var values: List<MiniStatementTransaction>
) :
    SimpleBindingAdapter<MiniStatementTransaction, ItemMiniStatementBinding>(R.layout.item_mini_statement) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = values[position]

        transaction.credit = transaction.credit ?: 0.0
        transaction.debit = transaction.debit ?: 0.0

        val amount = transaction.credit!! - transaction.debit!!

        holder.binding.amountTv.text = "NGN${amount}"
        holder.binding.narrationTv.text = transaction.narration
        holder.binding.timeOccurredTv.text =
            transaction.transactionDate?.toInstant(CREDIT_CLUB_DATE_PATTERN)?.toString("dd/MM/uuuu")
        holder.binding.toTv.text = null
        holder.binding.amountTv.setTextColor(context.resources.getColor(if (amount > 0) R.color.green else R.color.red))
    }
}