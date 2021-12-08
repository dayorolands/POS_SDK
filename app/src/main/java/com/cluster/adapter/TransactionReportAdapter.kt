package com.cluster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.appzonegroup.creditclub.pos.Platform
import com.cluster.core.data.model.TransactionReport
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.SimpleAdapter
import com.cluster.core.util.*
import com.cluster.ui.R
import com.cluster.ui.databinding.*
import com.cluster.ui.databinding.ItemReport1Binding as ItemReport1Binding1


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class TransactionReportAdapter(
    override var values: List<TransactionReport.ReportItem>,
    private val transactionType: TransactionType
) :
    SimpleAdapter<TransactionReportAdapter.ViewHolder, TransactionReport.ReportItem>() {

    private var listener: OnPrintClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: ViewDataBinding = when (viewType) {
            1, 6 -> DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_report_1,
                parent,
                false
            )
            2 -> DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_report_2,
                parent,
                false
            )
            3 -> DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_report_3,
                parent,
                false
            )
            4 -> DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_report_4,
                parent,
                false
            )
            5 -> DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_report_5,
                parent,
                false
            )
            7 -> DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_report_7,
                parent,
                false
            )
            else -> DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_report_0,
                parent,
                false
            )
        }

        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return when (transactionType) {
            TransactionType.MiniStatement, TransactionType.KiaKiaToKiaKia -> 1

            TransactionType.FundsTransferCommercialBank,
            TransactionType.KiaKiaToSterlingBank,
            TransactionType.KiaKiaToOtherBanks,
            TransactionType.LocalFundsTransfer -> 2

            TransactionType.Registration -> 3

            TransactionType.BillsPayment,
            TransactionType.CashIn,
            TransactionType.CashOut,
            TransactionType.Recharge -> 4

            TransactionType.PINChange, TransactionType.PINReset -> 5

            TransactionType.BalanceEnquiry -> 6

            TransactionType.CollectionPayment -> 7

            else -> 0
        }
    }

    fun setOnPrintClickListener(listener: OnPrintClickListener) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        with(holder.binding) {
            when (this) {
                is ItemReport1Binding1 -> {
                    if (getItemViewType(position) == 6) {
                        toTv.visibility - View.GONE
                        idLabel.visibility - View.GONE
                    }

                    fromTv.text = item.to
                    toTv.text = item.to
                    dateTv.text = item.date?.replace("T", " ")
                    phoneNoTv.text = item.fromPhoneNumber
                }

                is ItemReport2Binding -> {
                    fromTv.text = item.to
                    amountTv.text = item.amount?.toCurrencyFormat()
                    dateTv.text = item.date?.replace("T", " ")
                    phoneNoTv.text = item.fromPhoneNumber

                    if ((transactionType == TransactionType.FundsTransferCommercialBank
                                || transactionType == TransactionType.LocalFundsTransfer)
                        && Platform.hasPrinter
                    ) {
                        printReceiptButton.visibility = View.VISIBLE
                        printReceiptButton.setOnClickListener {
                            listener?.onClick(item, transactionType)
                        }
                    } else {
                        printReceiptButton.visibility = View.GONE
                    }
                }

                is ItemReport3Binding -> {
                    fromTv.text = item.to
                    amountTv.text = item.amount?.toCurrencyFormat()
                    dateTv.text = item.date?.replace("T", " ")
                    phoneNoTv.text = item.fromPhoneNumber
                    customerNameTv.text = item.customerName
                }

                is ItemReport4Binding -> {
                    fromTv.text = item.to
                    productNameTv.text = item.productName
                    dateTv.text = item.date?.replace("T", " ")
                    amountTv.text = item.amount?.toCurrencyFormat()
                    customerPhoneNoTv.text = item.customerPhone
                    customerNameTv.text = item.customerName

                    if (Platform.hasPrinter) {
                        printReceiptButton.visibility = View.VISIBLE
                        printReceiptButton.setOnClickListener {
                            listener?.onClick(item, transactionType)
                        }
                    } else printReceiptButton.visibility = View.GONE
                }

                is ItemReport5Binding -> {
                    fromTv.text = item.to
                    idTv.text = item.id.toString()
                    dateTv.text = item.date?.replace("T", " ")
                    phoneNoTv.text = item.fromPhoneNumber
                }

                is ItemReport7Binding -> {
                    amountTv.text = item.amount?.toCurrencyFormat()
                    idTv.text = item.id.toString()
                    dateTv.text = item.date?.replace("T", " ")
                    phoneNoTv.text = item.fromPhoneNumber

                    if (Platform.hasPrinter) {
                        printReceiptButton.visibility = View.VISIBLE
                        printReceiptButton.setOnClickListener {
                            listener?.onClick(item, transactionType)
                        }
                    } else printReceiptButton.visibility = View.GONE
                }

                is ItemReport0Binding -> {
                    timeOccurredTv.text = item.date?.toInstant()?.toString("dd/MM/uuuu")
                    customerNameTv.text = item.customerName
                    phoneNoTv.text = item.customerPhone
                    amountTv.text = item.amount?.toCurrencyFormat()
                }
            }
        }
    }

    inner class ViewHolder(val binding: ViewDataBinding) : SimpleAdapter.ViewHolder(binding.root)

    fun interface OnPrintClickListener {
        fun onClick(item: TransactionReport.ReportItem, type: TransactionType)
    }
}