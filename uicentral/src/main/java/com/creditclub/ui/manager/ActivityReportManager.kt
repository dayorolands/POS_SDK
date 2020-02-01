package com.creditclub.ui.manager

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.creditclub.core.data.request.POSTransactionReportRequest
import com.creditclub.core.type.TransactionGroup
import com.creditclub.core.type.TransactionStatus
import com.creditclub.core.type.TransactionType
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toString
import com.creditclub.ui.adapter.PosReportAdapter
import com.creditclub.ui.adapter.TransactionReportAdapter
import com.creditclub.ui.databinding.ActivityReportBinding
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.Period


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class ActivityReportManager(
    override val activity: CreditClubActivity,
    override val binding: ActivityReportBinding,
    private val transactionGroup: TransactionGroup
) : DataBindingActivityManager<ActivityReportBinding>(activity) {

    private var startIndex = 0
    private val maxSize = 20
    private var selectedTransactionType = transactionGroup.first()
    private var selectedTransactionStatus = TransactionStatus.Successful
    private var totalCount = 0
    private var transactionAdapter = TransactionReportAdapter(emptyList(), selectedTransactionType)
    private var posReportAdapter = PosReportAdapter(emptyList())

    private var endDate = LocalDate.now()
    private var startDate = endDate.minusDays(7)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.content.container.layoutManager = LinearLayoutManager(activity)

        val reportTypeOptions = transactionGroup.map { DialogOptionItem(it.label) }
        val transactionStatusOptions = TransactionStatus.values().map { DialogOptionItem(it.label) }

        binding.content.endDateContentTv.text = endDate.toString("uuuu-MM-dd")
        binding.content.startDateContentTv.text = startDate.toString("uuuu-MM-dd")

        binding.content.startDateLayout.setOnClickListener {
            val dateParams = DateInputParams(
                "Select start date",
                maxDate = LocalDate.now()
            )
            showDateInput(dateParams) {
                onSubmit { date ->
                    startDate = date
                    binding.content.startDateContentTv.text = date.toString("uuuu-MM-dd")
                }
            }
        }

        binding.content.endDateLayout.setOnClickListener {
            val dateParams = DateInputParams(
                "Select end date",
                maxDate = LocalDate.now()
            )

            showDateInput(dateParams) {
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

        binding.content.transactionStatusLayout.setOnClickListener {
            showOptions("Filter by status", transactionStatusOptions) {
                onSubmit {
                    startIndex = 0
                    selectedTransactionStatus = TransactionStatus.values()[it]
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

        binding.content.transactionStatusContentTv.text = selectedTransactionStatus.label
    }

    private fun fetchReport() {
        with(activity) {
            if (Period.between(startDate, endDate).days > 7) {
                showError("Date range cannot be more than 7 days")
                return
            }

            mainScope.launch {

                when (selectedTransactionType) {
                    TransactionType.POSCashOut -> {

                        val request = POSTransactionReportRequest().apply {
                            agentPhoneNumber = localStorage.agentPhone
                            institutionCode = localStorage.institutionCode
                            from = binding.content.startDateContentTv.text.toString()
                            to = binding.content.endDateContentTv.text.toString()
                            status = selectedTransactionStatus.code
                        }

                        request.startIndex = "$startIndex"
                        request.maxSize = "$maxSize"

                        showProgressBar("Getting POS transactions")
                        val (response, error) = safeRunIO {
                            creditClubMiddleWareAPI.reportService.getPOSTransactions(request)
                        }
                        hideProgressBar()

                        if (error != null) return@launch showError(error)
                        response ?: return@launch showInternalError()

                        posReportAdapter.setData(response.reports?.toList())
                    }
                    else -> {

                        showProgressBar("Getting transactions")
                        val (response, error) = safeRunIO {
                            creditClubMiddleWareAPI.reportService.getTransactions(
                                localStorage.agentPhone,
                                localStorage.institutionCode,
                                selectedTransactionType.code,
                                binding.content.startDateContentTv.text.toString(),
                                binding.content.endDateContentTv.text.toString(),
                                selectedTransactionStatus.code,
                                startIndex,
                                maxSize
                            )
                        }
                        hideProgressBar()

                        if (error != null) return@launch showError(error)
                        response ?: return@launch showInternalError()

                        transactionAdapter.setData(response.reports?.toList())
                    }
                }

                render()
            }
        }
    }

    private fun setAdapter() {
        if (selectedTransactionType == TransactionType.POSCashOut) {
            posReportAdapter = PosReportAdapter(emptyList())
            binding.content.container.adapter = posReportAdapter
        } else {
            transactionAdapter = TransactionReportAdapter(emptyList(), selectedTransactionType)
            binding.content.container.adapter = transactionAdapter
        }
    }
}