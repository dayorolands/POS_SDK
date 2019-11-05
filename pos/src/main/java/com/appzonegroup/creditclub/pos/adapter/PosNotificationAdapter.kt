package com.appzonegroup.creditclub.pos.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.databinding.FragmentTransactionBinding
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.appzonegroup.creditclub.pos.widget.PosNotificationListener
import com.appzonegroup.creditclub.pos.widget.PosNotificationListenerBlock

class PosNotificationAdapter(
    private var values: List<PosNotification>,
    listenerBlock: PosNotificationListenerBlock? = null
) : RecyclerView.Adapter<PosNotificationAdapter.ViewHolder>() {

    private var listener: PosNotificationListener? = null

    init {
        if (listenerBlock != null) {
            listener = PosNotificationListener.create(listenerBlock)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<FragmentTransactionBinding>(
            LayoutInflater.from(parent.context),
            R.layout.fragment_transaction,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.id.text = "${item.id}"

        holder.content.text =
            "${item.reference} ${item.maskedPAN} ${CurrencyFormatter.format(item.amount?.times(100).toString())}"
        holder.date.text = item.paymentDate

        with(holder.binding) {
            root.tag = item

            btnSettle.setOnClickListener {
                listener?.settle(item)
            }
        }
    }

    fun setData(newData: List<PosNotification>?) {
        this.values = newData ?: emptyList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val binding: FragmentTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        val id: TextView = binding.itemNumber
        val content: TextView = binding.content
        val date: TextView = binding.date

        override fun toString(): String {
            return super.toString() + " '" + content.text + "'"
        }
    }
}
