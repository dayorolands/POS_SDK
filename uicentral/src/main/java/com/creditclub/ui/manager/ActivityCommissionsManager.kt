package com.creditclub.ui.manager

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.creditclub.core.type.PaymentStatus
import com.creditclub.core.type.TransactionGroup
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toString
import com.creditclub.ui.adapter.CommissionReportAdapter
import com.creditclub.ui.databinding.ActivityCommissionsBinding
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class ActivityCommissionsManager(
    override val activity: CreditClubActivity,
    override val binding: ActivityCommissionsBinding,
    private val transactionGroup: TransactionGroup
) : DataBindingActivityManager<ActivityCommissionsBinding>(activity) {

    private var startIndex = 0
    private val maxSize = 20
    private var totalCount = 0

    private var selectedTransactionType = transactionGroup.first()
    private var selectedPaymentStatus = PaymentStatus.Successful

    private var commissionAdapter = CommissionReportAdapter(emptyList())

    private val reportTypeOptions = transactionGroup.map { DialogOptionItem(it.label) }
    private val paymentStatusOptions = PaymentStatus.values().map { DialogOptionItem(it.label) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.content.container.layoutManager = LinearLayoutManager(activity)

        val today = LocalDate.now()

        binding.content.endDateContentTv.text = today.toString("uuuu-MM-dd")
        binding.content.startDateContentTv.text =
            today.minusMonths(3).toString("uuuu-MM-dd")

        binding.content.startDateLayout.setOnClickListener {
            showDateInput(DateInputParams("Select start date", maxDate = today)) {
                onSubmit { date ->
                    binding.content.startDateContentTv.text =
                        date.toString("uuuu-MM-dd")
                }
            }
        }

        binding.content.endDateLayout.setOnClickListener {
            showDateInput(DateInputParams("Select end date", maxDate = today)) {
                onSubmit { date ->
                    binding.content.endDateContentTv.text = date.toString("uuuu-MM-dd")
                }
            }
        }

        binding.content.nextButton.setOnClickListener {
            if (startIndex + maxSize >= totalCount) {
                showError("There is no more data to display")
                return@setOnClickListener
            }
            startIndex += maxSize

            render()
            fetchReport()
        }

        binding.content.prevButton.setOnClickListener {
            if (startIndex >= maxSize) {
                startIndex -= maxSize
            }

            render()
            fetchReport()
        }

        binding.content.reportTypeLayout.setOnClickListener {
            showOptions("Report types", reportTypeOptions) {
                onSubmit {
                    startIndex = 0
                    selectedTransactionType = transactionGroup[it]
                    setAdapter()
                    render()
                    fetchReport()
                }
            }
        }

        binding.content.paymentStatusLayout.setOnClickListener {
            showOptions("Filter by status", paymentStatusOptions) {
                onSubmit {
                    startIndex = 0
                    selectedPaymentStatus = PaymentStatus.values()[it]
                    setAdapter()
                    render()
                    fetchReport()
                }
            }
        }

        binding.content.refreshButton.setOnClickListener {
            startIndex = 0

            fetchReport()
        }

        binding.content.home.setOnClickListener {
            activity.onBackPressed()
        }

        setAdapter()
        render()
        fetchReport()
    }

    override fun render() {
        super.render()
//
//        if (startIndex + maxSize < totalCount) {
//            binding.content.nextButton.show()
//        } else {
//            binding.content.nextButton.hide()
//        }
//
//        if (startIndex >= maxSize) {
//            binding.content.prevButton.show()
//        } else {
//            binding.content.prevButton.hide()
//        }

        binding.content.nextButton.visibility =
            if (startIndex + maxSize < totalCount) View.VISIBLE else View.INVISIBLE

        binding.content.prevButton.visibility =
            if (startIndex >= maxSize) View.VISIBLE else View.INVISIBLE

        binding.content.reportTypeContentTv.text = selectedTransactionType.label

        binding.content.paymentStatusContentTv.text = selectedPaymentStatus.label
    }

    private fun fetchReport() {
        with(activity) {
            mainScope.launch {

                showProgressBar("Getting commissions")
                val (response, error) = safeRunIO {
                    creditClubMiddleWareAPI.commissionService.getTransactions(
                        localStorage.agentPhone,
                        localStorage.institutionCode,
                        selectedTransactionType.code,
                        binding.content.startDateContentTv.text.toString(),
                        binding.content.endDateContentTv.text.toString(),
                        selectedPaymentStatus.code,
                        startIndex,
                        maxSize
                    )
                }
                hideProgressBar()

                if (error != null) return@launch showError(error)
                response ?: return@launch showInternalError()

                commissionAdapter.setData(response.reports?.toList())

                render()
            }
        }
    }

    private fun setAdapter() {
        commissionAdapter = CommissionReportAdapter(emptyList())
        binding.content.container.adapter = commissionAdapter
    }
}