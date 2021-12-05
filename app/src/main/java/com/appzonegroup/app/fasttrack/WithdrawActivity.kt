package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.appzonegroup.app.fasttrack.databinding.ActivityWithdrawBinding
import com.appzonegroup.app.fasttrack.fragment.WithdrawalViewModel
import com.appzonegroup.app.fasttrack.receipt.withdrawalReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.ClusterObjectBox
import com.creditclub.core.data.TRANSACTIONS_CLIENT
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.PendingTransaction
import com.creditclub.core.data.prefs.newTransactionReference
import com.creditclub.core.data.request.WithdrawalRequest
import com.creditclub.core.type.TokenType
import com.creditclub.core.type.TransactionType
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.core.util.finishOnClose
import com.creditclub.core.util.toString
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import java.time.Instant

/**
 * Created by DELL on 2/27/2017.
 */

class WithdrawActivity : CustomerBaseActivity(flowName = "withdrawal") {

    private val binding by contentView<WithdrawActivity, ActivityWithdrawBinding>(
        R.layout.activity_withdraw
    )
    override val functionId = FunctionIds.TOKEN_WITHDRAWAL
    private val tokenWithdrawalConfig by lazy { institutionConfig.flows.tokenWithdrawal }
    private val retrievalReferenceNumber by lazy {
        localStorage.newTransactionReference()
    }

    private val viewModel: WithdrawalViewModel by viewModels()
    private val staticService: StaticService by retrofitService()
    private val transactionService: StaticService by retrofitService(TRANSACTIONS_CLIENT)
    private val clusterObjectBox: ClusterObjectBox by inject()

    override fun onCustomerReady(savedInstanceState: Bundle?) {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        with(viewModel) {
            hasExternalToken.observe(this@WithdrawActivity) {
                showPhoneNumberInput.value =
                    hasExternalToken.value == true && accountInfo.value?.phoneNumber == null
            }
            accountInfo.observe(this@WithdrawActivity) { accountInfo ->
                binding.accountInfoEt.setText(accountInfo?.accountName)
                showPhoneNumberInput.value =
                    hasExternalToken.value == true && accountInfo?.phoneNumber == null
            }
        }

        viewModel.hasExternalToken.value = tokenWithdrawalConfig?.externalToken == true
        viewModel.accountInfo.value = accountInfo

        if (tokenWithdrawalConfig?.externalToken == true) {
            viewModel.tokenSent.value = true
            binding.withdrawalAmountEt.isEnabled = true
            binding.topLayout.isEnabled = false
            binding.sendTokenBtn.visibility = View.GONE
            binding.tokenBlock.visibility = View.VISIBLE
        }

        val chooseAnotherAccount = {
            requireAccountInfo(options = customerRequestOptions) {
                onSubmit { accountInfo ->
                    this@WithdrawActivity.accountInfo = accountInfo
                    viewModel.accountInfo.value = accountInfo
                }
            }
        }

        binding.accountInfoEt.run {
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) chooseAnotherAccount() }
            setOnClickListener { if (isFocused) chooseAnotherAccount() }
        }

        binding.sendTokenBtn.setOnClickListener {
            mainScope.launch { trySendToken() }
        }

        binding.withdrawBtn.setOnClickListener {
            mainScope.launch { onWithdrawClick() }
        }

        if (tokenWithdrawalConfig?.customerPin == true) {
            binding.customerPinEt.visibility = View.VISIBLE
        }
    }

    private suspend fun trySendToken() {
        val amount = viewModel.amountString.value?.trim { it <= ' ' }
        if (amount.isNullOrBlank()) {
            dialogProvider.showError(getString(R.string.please_enter_an_amount))
            return
        }

        try {
            amount.toDouble()
        } catch (ex: Exception) {
            dialogProvider.showError(getString(R.string.please_enter_a_valid_amount))
            return
        }

        viewModel.tokenSent.value = sendToken(accountInfo, TokenType.Withdrawal, amount.toDouble())

        binding.withdrawalAmountEt.isEnabled = false
        binding.topLayout.isEnabled = false
        binding.sendTokenBtn.visibility = View.GONE
        binding.tokenBlock.visibility = View.VISIBLE
    }

    private suspend fun onWithdrawClick() {

        if (viewModel.tokenSent.value != true) {
            dialogProvider.showError("No token has been sent to the customer. Click the \"Send Token\" button to continue")
            return
        }

        val amount = viewModel.amountString.value!!.trim { it <= ' ' }
        if (amount.isEmpty()) {
            indicateError("Enter an amount", binding.withdrawalAmountEt)
            return
        }

        try {
            amount.toDouble()
        } catch (ex: Exception) {
            indicateError("Please enter a valid amount", binding.withdrawalAmountEt)
            return
        }

        val phoneNumber = binding.customerPhoneNumberEt.value
        if (viewModel.showPhoneNumberInput.value == true && phoneNumber.length != 11) {
            indicateError("Please enter a valid phone number", binding.customerPhoneNumberEt)
            return
        }

        val customerPin: String
        if (tokenWithdrawalConfig?.customerPin == true) {
            customerPin = binding.customerPinEt.value

            if (customerPin.isEmpty()) {
                indicateError("Please enter a valid customer PIN", binding.customerPinEt)
                return
            }

            if (customerPin.length != 4) {
                indicateError("Customer PIN must be 4 digits", binding.customerPinEt)
                return
            }
        } else {
            customerPin = "0000"
        }

        val token = binding.tokenEt.value
        if (token.length < 5) {
            indicateError("Please enter a valid token", binding.tokenEt)
            return
        }
        val request = WithdrawalRequest(
            agentPhoneNumber = localStorage.agent?.phoneNumber,
            customerAccountNumber = accountInfo.number,
            amount = amount,
            institutionCode = localStorage.institutionCode,
            token = token,
            customerPin = customerPin,
            retrievalReferenceNumber = retrievalReferenceNumber,
            geoLocation = localStorage.lastKnownLocation,
            deviceNumber = localStorage.deviceNumber,
            additionalInformation = Json.encodeToString(
                WithdrawalRequest.Additional.serializer(),
                WithdrawalRequest.Additional(
                    customerPhoneNumber = accountInfo.phoneNumber ?: viewModel.phoneNumber.value,
                )
            ),
        )
        renderTransactionSummary(
            amount = amount.toDouble(),
            onProceed = {
                val agentPin =
                    dialogProvider.getPin("Enter agent PIN") ?: return@renderTransactionSummary
                withdraw(request.copy(agentPin = agentPin))
            },
            fetchFeeAgent = {
                staticService.getWithdrawalFee(request)
            }
        )
    }

    private suspend fun withdraw(request: WithdrawalRequest) {
        dialogProvider.showProgressBar("Processing transaction")
        val requestTime = Instant.now()
        val pendingTransactionsBox: Box<PendingTransaction> = clusterObjectBox.boxStore.boxFor()
        val pendingTransaction = PendingTransaction(
            transactionType = TransactionType.CashOut,
            requestJson = defaultJson.encodeToString(
                WithdrawalRequest.serializer(),
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
            fetcher = { transactionService.withdrawal(request) },
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
        if (response == null) return dialogProvider.showError(
            "Transaction failed. Please try again later",
            finishOnClose
        )

        val receipt = withdrawalReceipt(
            this@WithdrawActivity,
            request,
            accountInfo,
            Instant.now().toString("dd-MM-yyyy hh:mm"),
            isSuccessful = response.isSuccessful,
            reason = response.responseMessage,
        )
        renderReceiptDetails(receipt)
    }
}



