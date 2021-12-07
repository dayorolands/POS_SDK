package com.cluster

import android.os.Bundle
import android.widget.EditText
import com.cluster.databinding.ActivityDepositBinding
import com.cluster.receipt.depositReceipt
import com.cluster.utility.FunctionIds
import com.creditclub.core.data.ClusterObjectBox
import com.creditclub.core.data.TRANSACTIONS_CLIENT
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.PendingTransaction
import com.creditclub.core.data.prefs.newTransactionReference
import com.creditclub.core.data.request.DepositRequest
import com.creditclub.core.type.TransactionType
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.delegates.defaultJson
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant

class DepositActivity : CustomerBaseActivity() {
    private val binding by contentView<DepositActivity, ActivityDepositBinding>(R.layout.activity_deposit)
    override val functionId = FunctionIds.DEPOSIT
    private val staticService: StaticService by retrofitService()
    private val transactionService: StaticService by retrofitService(TRANSACTIONS_CLIENT)
    private val retrievalReferenceNumber by lazy {
        localStorage.newTransactionReference()
    }
    private val clusterObjectBox: ClusterObjectBox by inject()

    override fun onCustomerReady(savedInstanceState: Bundle?) {

        binding.accountsSpinner.isEnabled = false

        binding.accountInfoEt.run {
            setText(accountInfo.accountName)

            val chooseAnotherAccount = {
                requireAccountInfo(options = customerRequestOptions) {
                    onSubmit { newInfo ->
                        accountInfo = newInfo

                        setText(accountInfo.accountName)
                    }
                }
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) chooseAnotherAccount()
            }

            setOnClickListener {
                if (isFocused) chooseAnotherAccount()
            }
        }

        binding.depositBtn.setOnClickListener {
            mainScope.launch { onDepositClick() }
        }
    }

    private suspend fun onDepositClick() {
        val amount = binding.depositAmountEt.value
        if (amount.isEmpty()) {
            indicateError("Amount should be greater than 0", binding.depositAmountEt)
            return
        }

        try {
            val money = java.lang.Double.parseDouble(amount)

            if (money <= 0) {
                indicateError("Please enter an amount greater than 0", binding.depositAmountEt)
                return
            }
        } catch (ex: Exception) {
            indicateError("Invalid input. Please enter a number", binding.depositAmountEt)
            return
        }

        val depositRequest = DepositRequest(
            agentPhoneNumber = localStorage.agentPhone,
            institutionCode = localStorage.institutionCode,
            customerAccountNumber = accountInfo.number,
            amount = amount,
            geoLocation = localStorage.lastKnownLocation,
            retrievalReferenceNumber = retrievalReferenceNumber,
            deviceNumber = localStorage.deviceNumber,
        )
        renderTransactionSummary(
            amount = amount.toDouble(),
            onProceed = {
                val agentPin =
                    dialogProvider.getPin(R.string.agent_pin) ?: return@renderTransactionSummary
                attemptDeposit(depositRequest.copy(agentPin = agentPin))
            },
            fetchFeeAgent = {
                staticService.getDepositFee(request = depositRequest)
            }
        )
    }

    private suspend fun attemptDeposit(request: DepositRequest) {
        dialogProvider.showProgressBar("Processing Transaction", "Please wait...")
        val requestTime = Instant.now()
        val pendingTransactionsBox: Box<PendingTransaction> = clusterObjectBox.boxStore.boxFor()
        val pendingTransaction = PendingTransaction(
            transactionType = TransactionType.CashIn,
            requestJson = defaultJson.encodeToString(
                DepositRequest.serializer(),
                request,
            ),
            accountName = accountInfo.accountName,
            accountNumber = request.customerAccountNumber,
            amount = request.amount.toDouble(),
            reference = request.retrievalReferenceNumber,
            createdAt = requestTime,
            lastCheckedAt = null,
        )
        val (response, error) = executeTransaction(
            fetcher = { transactionService.deposit(request) },
            reFetcher = {
                transactionService.getTransactionStatusByReferenceNumber(
                    deviceNumber = localStorage.deviceNumber,
                    retrievalReferenceNumber = request.retrievalReferenceNumber,
                    institutionCode = localStorage.institutionCode,
                )
            },
            pendingTransaction = pendingTransaction,
            pendingTransactionsBox = pendingTransactionsBox,
            dialogProvider = dialogProvider,
        )
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        if (response == null) return showNetworkError(finishOnClose)

        val receipt = depositReceipt(
            context = this@DepositActivity,
            request = request,
            accountInfo = accountInfo,
            isSuccessful = response.isSuccessful,
            reason = response.responseMessage,
            transactionDate = requestTime
                .toString(CREDIT_CLUB_REQUEST_DATE_PATTERN)
                .replace("T", " "),
        )
        renderReceiptDetails(receipt)
    }

    override fun indicateError(message: String?, view: EditText?) {
        view?.error = message
        view?.requestFocus()
    }
}