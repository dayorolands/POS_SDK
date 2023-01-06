package com.cluster

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cluster.adapter.PWTReportAdapter
import com.cluster.adapter.TransactionReportAdapter
import com.cluster.databinding.ActivityReportBinding
import com.cluster.receipt.*
import com.cluster.core.data.api.CollectionsService
import com.cluster.core.data.api.ReportService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.*
import com.cluster.core.data.request.*
import com.cluster.core.type.TransactionStatus
import com.cluster.core.type.TransactionType
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DateInputParams
import com.cluster.core.ui.widget.DialogOptionItem
import com.cluster.core.util.*
import com.cluster.pos.printer.PosPrinter
import com.cluster.adapter.PosReportAdapter
import com.cluster.ui.dataBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.Period

class ReportActivity : CreditClubActivity(R.layout.activity_report) {
    private val binding: ActivityReportBinding by dataBinding()
    private var startIndex = 0
    private val maxSize = 20
    private var totalCount = 0
    private val posPrinter: PosPrinter by inject { parametersOf(this, dialogProvider) }
    private val transactionGroup = institutionConfig.transactionTypes
    private var selectedTransactionType = if(transactionGroup.isEmpty()) TransactionType.Nothing else transactionGroup.first()
    private var selectedTransactionStatus = TransactionStatus.Successful
    private var transactionAdapter = TransactionReportAdapter(emptyList(), selectedTransactionType)
    private var posReportAdapter = PosReportAdapter(emptyList())
    private var pwtReportAdapter = PWTReportAdapter(emptyList())
    private var endDate = LocalDate.now()
    private var startDate = endDate.minusDays(0)
    private val reportService: ReportService by retrofitService()
    private val collectionsService: CollectionsService by retrofitService()

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
                dialogProvider.showError("There is no more data to display")
                return@setOnClickListener
            }
            startIndex += maxSize

            render()
            mainScope.launch { fetchReport() }
        }

        binding.content.prevButton.setOnClickListener {
            if (startIndex >= maxSize) {
                startIndex -= maxSize
            }

            render()
            mainScope.launch { fetchReport() }
        }

        binding.content.reportTypeLayout.setOnClickListener {
            dialogProvider.showOptions("Report types", reportTypeOptions) {
                onSubmit {
                    startIndex = 0
                    selectedTransactionType = transactionGroup[it]
                    setAdapter()
                    render()
                    mainScope.launch { fetchReport() }
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
                    mainScope.launch { fetchReport() }
                }
            }
        }

        binding.content.refreshButton.setOnClickListener {
            startIndex = 0

            mainScope.launch { fetchReport() }
        }

        binding.content.home.setOnClickListener {
            onBackPressed()
        }

        setAdapter()
        render()
        mainScope.launch { fetchReport() }
    }

    private fun render() {
        binding.content.nextButton.visibility =
            if (startIndex + maxSize < totalCount) View.VISIBLE else View.INVISIBLE

        binding.content.prevButton.visibility =
            if (startIndex >= maxSize) View.VISIBLE else View.INVISIBLE

        binding.content.reportTypeContentTv.text = selectedTransactionType.label

        binding.content.transactionStatusContentTv.text = selectedTransactionStatus.label
    }

    private suspend fun fetchReport() {
        if (Period.between(startDate, endDate).days > 7) {
            dialogProvider.showError("Date range cannot be more than 7 days")
            return
        }

        when (selectedTransactionType) {
            TransactionType.PayWithTransfer -> {
                val request = PWTTransactionReportRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    agentCode = localStorage.agent?.agentCode,
                    institutionCode = localStorage.institutionCode,
                    from = binding.content.startDateContentTv.value,
                    to = binding.content.endDateContentTv.value,
                    status = selectedTransactionStatus.code,
                    startIndex = "$startIndex",
                    maxSize = "$maxSize"
                )

                dialogProvider.showProgressBar("Getting Pay With Transfer transactions")
                val (response, error) = safeRunIO {
                    reportService.getPWTTransactions(request)
                }
                dialogProvider.hideProgressBar()
                if (error != null) return dialogProvider.showError(error)
                response ?: return dialogProvider.showInternalError()

                binding.content.totalCountContentTv.text = response.totalCount.toString()
                binding.content.totalVolumeContentTv.text = "--"
                pwtReportAdapter.setData(response.payWithTransferReport?.toList())
            }

            TransactionType.POSCashOut -> {

                val request = POSTransactionReportRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    from = binding.content.startDateContentTv.value,
                    to = binding.content.endDateContentTv.value,
                    status = selectedTransactionStatus.code,
                    startIndex = "$startIndex",
                    maxSize = "$maxSize",
                )

                dialogProvider.showProgressBar("Getting POS transactions")
                val (response, error) = safeRunIO {
                    reportService.getPOSTransactions(request)
                }
                dialogProvider.hideProgressBar()

                if (error != null) return dialogProvider.showError(error)
                response ?: return dialogProvider.showInternalError()

                binding.content.totalCountContentTv.text = response.totalCount.toString()
                binding.content.totalVolumeContentTv.text = response.totalAmount.toCurrencyFormat()
                posReportAdapter.setData(response.reports?.toList())
            }
            else -> {
                dialogProvider.showProgressBar("Getting transactions")
                val (response, error) = safeRunIO {
                    reportService.getTransactions(
                        localStorage.agentPhone,
                        localStorage.institutionCode,
                        selectedTransactionType.code,
                        binding.content.startDateContentTv.text.toString(),
                        binding.content.endDateContentTv.text.toString(),
                        selectedTransactionStatus.code,
                        startIndex,
                        maxSize,
                    )
                }
                dialogProvider.hideProgressBar()

                if (error != null) return dialogProvider.showError(error)
                response ?: return dialogProvider.showInternalError()

                binding.content.totalCountContentTv.text = response.totalCount.toString()
                binding.content.totalVolumeContentTv.text = "--"
                transactionAdapter.setData(response.reports?.toList())
            }
        }

        render()
    }

    private fun setAdapter() {
        if(selectedTransactionType == TransactionType.PayWithTransfer){
            pwtReportAdapter = PWTReportAdapter(emptyList())
            binding.content.container.adapter = pwtReportAdapter
            pwtReportAdapter.setOnPrintClickListener { item ->
                mainScope.launch {
                    printPayWithTransferReceipt(item)
                }
            }
        }
        else if (selectedTransactionType == TransactionType.POSCashOut) {
            posReportAdapter = PosReportAdapter(emptyList())
            binding.content.container.adapter = posReportAdapter
            posReportAdapter.setOnPrintClickListener{item, type ->
                mainScope.launch {
                    printPosCashoutReceipt(item, type)
                }
            }
        } else {
            transactionAdapter =
                TransactionReportAdapter(
                    emptyList(),
                    selectedTransactionType
                )
            binding.content.container.adapter = transactionAdapter
            transactionAdapter.setOnPrintClickListener { item, type ->
                mainScope.launch {
                    printReceipt(item, type)
                }
            }
        }
    }

    private suspend fun printPayWithTransferReceipt(item: PWTTransactionReportResponse.PayWithTransferReport){
        val pwtReceiptRequest = PWTReceiptRequest(
            virtualAccountNumber = item.virtualAccountNumber,
            agentPhoneNumber = item.agentPhoneNumber,
            agentCode = item.agentCode,
            narration = item.narration,
            customerAccountName = item.customerAccountName,
            customerAcctNumber = item.customerAcctNumber,
            amountReceived = item.amountReceived,
            customerName = item.customerName,
            agentAccount = item.agentAccount,
            agentAccountName = item.agentAccountName,
            date = item.date,
            rrn = item.rrn,
            expectedAmount = item.expectedAmount
        )
        val responseCode =  when(selectedTransactionStatus) {
            TransactionStatus.Successful -> "00"
            TransactionStatus.Failed -> "06"
            TransactionStatus.Pending -> "24"
            TransactionStatus.NotFound -> "XX"
            else -> {
                "06"
            }
        }

        posPrinter.print(
            payWithTransferReceipt(
                context = this,
                request = pwtReceiptRequest,
                isSuccessful = selectedTransactionStatus == TransactionStatus.Successful,
                reason = selectedTransactionStatus.label,
                responseCode = responseCode
            )
        )
    }


    private suspend fun printPosCashoutReceipt(item: PosTransactionReport.Report, type: TransactionType){
        when(type) {
            TransactionType.POSCashOut -> {
                val posCashoutRequest = POSCashoutRequest(
                    agentPhoneNumber = item.agentPhoneNumber,
                    agentCode = localStorage.agent?.agentCode,
                    amount = item.transactionAmount,
                    transactionReference = item.transactionReference,
                    deviceNumber = localStorage.deviceNumber,
                    maskedPan = item.maskedPan,
                    transactionStan = item.transactionStan,
                    cardType = item.cardType,
                    expiryDate = item.expiryDate,
                    retrievalReferenceNumber = item.retrievalReferenceNumber,
                    cardHolderName = item.cardHolderName,
                )
                posPrinter.print(
                    posCashoutReceipt(
                        context = this,
                        request = posCashoutRequest,
                        transactionDate = item.transactionDateTime?.toInstant(CREDIT_CLUB_DATE_PATTERN).toString().replace("T"," "),
                        settlementDate = item.settlementDate?.toInstant(CREDIT_CLUB_DATE_PATTERN).toString().replace("T", " "),
                        isSuccessful = selectedTransactionStatus == TransactionStatus.Successful,
                        reason = selectedTransactionStatus.label,
                        responseCode = item.responseCode
                    )
                )
            }
            else -> {}
        }
    }

    private suspend fun printReceipt(item: TransactionReport.ReportItem, type: TransactionType) {
        when (type) {
            TransactionType.CollectionPayment -> {
                dialogProvider.showProgressBar("Loading collection reference")
                val (response, error) = safeRunIO {
                    collectionsService.verifyCollectionPayment(
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
                val collectionReportRequest = CollectionReportRequest(
                    deviceNumber = localStorage.deviceNumber,
                    retrievalReferenceNumber = item.uniqueReference,
                    customerName = item.customerName
                )
                posPrinter.print(collectionReportReceipt(this, response, collectionReportRequest))
            }
            TransactionType.FundsTransferCommercialBank,
            TransactionType.LocalFundsTransfer,
            -> {
                val fundsTransferRequest = FundsTransferRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    agentCode = localStorage.agent?.agentCode,
                    institutionCode = localStorage.institutionCode,
                    beneficiaryAccountName = item.customerName,
                    beneficiaryAccountNumber = item.to,
                    amountInNaira = item.amount ?: 0.0,
                    externalTransactionReference = item.uniqueReference,
                    retrievalReferenceNumber = item.uniqueReference,
                    deviceNumber = localStorage.deviceNumber,
                )
                val receipt = fundsTransferReceipt(
                    context = this,
                    request = fundsTransferRequest,
                    transactionDate = item.date?.replace("T", " ") ?: "",
                    isSuccessful = selectedTransactionStatus == TransactionStatus.Successful,
                    reason = selectedTransactionStatus.label,
                )
                posPrinter.print(receipt)
            }
            TransactionType.BillsPayment,
            TransactionType.Recharge,
            -> {
                val payBillRequest = PayBillRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    agentCode = localStorage.agent?.agentCode,
                    institutionCode = localStorage.institutionCode,
                    customerName = item.customerName,
                    customerPhone = item.customerPhone,
                    customerEmail = null,
                    amount = item.amount.toString(),
                    customerDepositSlipNumber = item.uniqueReference,
                    retrievalReferenceNumber = item.uniqueReference,
                    deviceNumber = localStorage.deviceNumber,
                    geolocation = null,
                    isRecharge = selectedTransactionType == TransactionType.Recharge,
                    validationCode = null,
                    agentPin = null,
                    merchantBillerIdField = null,
                    billerCategoryName = null,
                    billerCategoryID = null,
                    billItemID = null,
                    customerId = null,
                    accountNumber = null,
                    billerName = null,
                    paymentItemCode = null,
                    paymentItemName = item.productName,
                )
                val response = PayBillResponse(
                    isSuccessful = selectedTransactionStatus == TransactionStatus.Successful,
                    responseMessage = selectedTransactionStatus.label,
                )
                posPrinter.print(
                    billsPaymentReceipt(
                        context = this,
                        transactionDate = item.date?.replace("T", " ") ?: "",
                        request = payBillRequest,
                        response = response,
                    )
                )
            }
            TransactionType.CashIn -> {
                val depositRequest = DepositRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    customerAccountNumber = item.to!!,
                    amount = item.amount.toString(),
                    uniqueReferenceID = item.uniqueReference,
                    retrievalReferenceNumber = item.uniqueReference,
                    deviceNumber = localStorage.deviceNumber,
                )
                posPrinter.print(
                    depositReceipt(
                        context = this,
                        request = depositRequest,
                        accountInfo = AccountInfo(
                            accountName = item.customerName ?: "",
                            number = item.to ?: "",
                            phoneNumber = item.customerPhone,
                            isSuccessful = true,
                        ),
                        isSuccessful = selectedTransactionStatus == TransactionStatus.Successful,
                        reason = selectedTransactionStatus.label,
                        transactionDate = item.date?.replace("T", " ") ?: "",
                    )
                )
            }
            TransactionType.CashOut -> {
                val withdrawalRequest = WithdrawalRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    customerAccountNumber = item.to!!,
                    amount = item.amount.toString(),
                    retrievalReferenceNumber = item.uniqueReference,
                    deviceNumber = localStorage.deviceNumber,
                )
                posPrinter.print(
                    withdrawalReceipt(
                        context = this,
                        request = withdrawalRequest,
                        accountInfo = AccountInfo(
                            accountName = item.customerName ?: "",
                            number = item.to ?: "",
                            phoneNumber = item.customerPhone,
                            isSuccessful = true,
                        ),
                        isSuccessful = selectedTransactionStatus == TransactionStatus.Successful,
                        reason = selectedTransactionStatus.label,
                        transactionDate = item.date?.replace("T", " ") ?: "",
                    )
                )
            }
            TransactionType.CrossBankTokenWithdrawal -> {
                val request = CrossBankRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    customerAccountNumber = item.to!!,
                    amount = item.amount.toString(),
                    retrievalReferenceNumber = item.uniqueReference,
                    deviceNumber = localStorage.deviceNumber,
                )
                posPrinter.print(
                    crossBankTokenReceipt(
                        context = this,
                        request = request,
                        transactionDate = item.date?.replace("T"," ") ?: "",
                        isSuccessful = selectedTransactionStatus == TransactionStatus.Successful,
                        reason = selectedTransactionStatus.label,
                        customerName = item.customerName
                    )
                )
            }
            else -> {
            }
        }
    }
}