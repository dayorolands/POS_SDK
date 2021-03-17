package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.adapter.TransactionReportAdapter
import com.appzonegroup.app.fasttrack.receipt.CollectionPaymentReceipt
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.model.TransactionReport
import com.creditclub.core.data.request.POSTransactionReportRequest
import com.creditclub.core.type.TransactionStatus
import com.creditclub.core.type.TransactionType
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.util.*
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.ui.adapter.PosReportAdapter
import com.creditclub.ui.dataBinding
import com.appzonegroup.app.fasttrack.databinding.ActivityReportBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.Period

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class ReportActivity : CreditClubActivity(R.layout.activity_report) {
    private val binding: ActivityReportBinding by dataBinding()
    private var startIndex = 0
    private val maxSize = 20
    private var totalCount = 0
    private val posPrinter: PosPrinter by inject { parametersOf(this, dialogProvider) }
    private val transactionGroup = institutionConfig.transactionTypes
    private var selectedTransactionType = transactionGroup.first()
    private var selectedTransactionStatus = TransactionStatus.Successful
    private var transactionAdapter = TransactionReportAdapter(emptyList(), selectedTransactionType)
    private var posReportAdapter = PosReportAdapter(emptyList())
    private var endDate = LocalDate.now()
    private var startDate = endDate.minusDays(7)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.content.container.layoutManager = LinearLayoutManager(this)

        val reportTypeOptions = transactionGroup.map { DialogOptionItem(it.label) }
        val transactionStatusOptions = TransactionStatus.values().map { DialogOptionItem(it.label) }

        binding.content.endDateContentTv.text = endDate.toString("uuuu-MM-dd")
        binding.content.startDateContentTv.text = startDate.toString("uuuu-MM-dd")

        binding.content.startDateLayout.setOnClickListener {
            val dateParams = DateInputParams(
                "Select start date",
                maxDate = LocalDate.now()
            )
            dialogProvider.showDateInput(dateParams) {
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

            dialogProvider.showDateInput(dateParams) {
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
            dialogProvider.showOptions("Report types", reportTypeOptions) {
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
            dialogProvider.showOptions("Filter by status", transactionStatusOptions) {
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
            onBackPressed()
        }

        setAdapter()
        render()
        fetchReport()
    }

    private fun render() {
        binding.content.nextButton.visibility =
            if (startIndex + maxSize < totalCount) View.VISIBLE else View.INVISIBLE

        binding.content.prevButton.visibility =
            if (startIndex >= maxSize) View.VISIBLE else View.INVISIBLE

        binding.content.reportTypeContentTv.text = selectedTransactionType.label

        binding.content.transactionStatusContentTv.text = selectedTransactionStatus.label
    }

    private fun fetchReport() {
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

    private fun setAdapter() {
        if (selectedTransactionType == TransactionType.POSCashOut) {
            posReportAdapter = PosReportAdapter(emptyList())
            binding.content.container.adapter = posReportAdapter
        } else {
            transactionAdapter =
                TransactionReportAdapter(
                    emptyList(),
                    selectedTransactionType
                )
            binding.content.container.adapter = transactionAdapter
            transactionAdapter.setOnPrintClickListener(object :
                TransactionReportAdapter.OnPrintClickListener {
                override fun onClick(item: TransactionReport.ReportItem, type: TransactionType) {
                    mainScope.launch {
                        printReceipt(item, type)
                    }
                }
            })
        }
    }

    private suspend fun printReceipt(item: TransactionReport.ReportItem, type: TransactionType) {
        when (type) {
            TransactionType.CollectionPayment -> {
                dialogProvider.showProgressBar("Loading collection reference")
                val (response, error) = safeRunIO {
                    creditClubMiddleWareAPI.collectionsService.verifyCollectionPayment(
                        localStorage.institutionCode,
                        item.uniqueReference,
                        null,
                        null
                    )
                }
                dialogProvider.hideProgressBar()

                if (error != null) return dialogProvider.showError(error)
                if (response == null) {
                    return dialogProvider.showError("An error occurred. Please try again later")
                }
                response.date = item.date?.toInstant(CREDIT_CLUB_DATE_PATTERN)
                if (response.isSuccessful == true) {
                    dialogProvider.showSuccessAndWait(response.responseMessage ?: "Success")
                } else {
                    dialogProvider.showErrorAndWait(response.responseMessage ?: "Error")
                }

                if (Platform.hasPrinter) {
                    posPrinter.print(CollectionPaymentReceipt(this, response))
                }
            }
            else -> {
            }
        }
    }
}