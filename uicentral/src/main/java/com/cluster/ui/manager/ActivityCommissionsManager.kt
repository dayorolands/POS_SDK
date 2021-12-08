package com.cluster.ui.manager

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cluster.core.data.api.CommissionService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.type.PaymentStatus
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DateInputParams
import com.cluster.core.ui.widget.DialogOptionItem
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toString
import com.cluster.ui.adapter.CommissionReportAdapter
import com.cluster.ui.databinding.ActivityCommissionsBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class ActivityCommissionsManager(
    override val activity: CreditClubActivity,
    override val binding: ActivityCommissionsBinding,
    private val transactionGroup: List<TransactionType>
) : DataBindingActivityManager<ActivityCommissionsBinding>(activity) {

    private var startIndex = 0
    private val maxSize = 20
    private var totalCount = 0

    private var selectedTransactionType = transactionGroup.first()
    private var selectedPaymentStatus = PaymentStatus.Successful

    private var commissionAdapter = CommissionReportAdapter(emptyList())

    private val reportTypeOptions = transactionGroup.map { DialogOptionItem(it.label) }
    private val paymentStatusOptions = PaymentStatus.values().map { DialogOptionItem(it.label) }

    private var endDate = LocalDate.now()
    private var startDate = endDate.minusDays(7)
    private var today = LocalDate.now()

    private val commissionService: CommissionService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.content.container.layoutManager = LinearLayoutManager(activity)

        binding.content.endDateContentTv.text = endDate.toString("uuuu-MM-dd")
        binding.content.startDateContentTv.text = startDate.toString("uuuu-MM-dd")

        binding.content.startDateLayout.setOnClickListener {
            showDateInput(DateInputParams("Select start date", maxDate = today)) {
                onSubmit { date ->
                    startDate = date
                    binding.content.startDateContentTv.text =
                        date.toString("uuuu-MM-dd")
                }
            }
        }

        binding.content.endDateLayout.setOnClickListener {
            showDateInput(DateInputParams("Select end date", maxDate = today)) {
                onSubmit { date ->
                    endDate = date
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

        binding.content.nextButton.visibility =
            if (startIndex + maxSize < totalCount) View.VISIBLE else View.INVISIBLE

        binding.content.prevButton.visibility =
            if (startIndex >= maxSize) View.VISIBLE else View.INVISIBLE

        binding.content.reportTypeContentTv.text = selectedTransactionType.label

        binding.content.paymentStatusContentTv.text = selectedPaymentStatus.label
    }

    private fun fetchReport() {
        with(activity) {
            if (Period.between(startDate, endDate).days > 7) {
                showError("Date range cannot be more than 7 days")
                return
            }

            mainScope.launch {

                showProgressBar("Getting commissions")
                val (response, error) = safeRunIO {
                    commissionService.getTransactions(
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