package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.appzonegroup.app.fasttrack.databinding.ActivityWithdrawBinding
import com.appzonegroup.app.fasttrack.fragment.WithdrawalViewModel
import com.appzonegroup.app.fasttrack.receipt.WithdrawalReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.request.WithdrawalRequest
import com.creditclub.core.type.TokenType
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import com.creditclub.pos.printer.PrinterStatus
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Created by DELL on 2/27/2017.
 */

class WithdrawActivity : CustomerBaseActivity(flowName = "withdrawal") {

    private val binding by contentView<WithdrawActivity, ActivityWithdrawBinding>(
        R.layout.activity_withdraw
    )
    override val functionId = FunctionIds.TOKEN_WITHDRAWAL
    private val tokenWithdrawalConfig by lazy { institutionConfig.flows.tokenWithdrawal }

    private val request = WithdrawalRequest().apply {
        retrievalReferenceNumber = generateRRN()
    }

    private val viewModel: WithdrawalViewModel by viewModels()

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

        viewModel.hasExternalToken.value = tokenWithdrawalConfig.externalToken
        viewModel.accountInfo.value = accountInfo

        if (tokenWithdrawalConfig.externalToken) {
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

        if (tokenWithdrawalConfig.customerPin) {
            binding.customerPinEt.visibility = View.VISIBLE
        }
    }

    private suspend fun trySendToken() {
        val amount = viewModel.amountString.value!!.trim { it <= ' ' }
        if (amount.isEmpty()) {
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

    private fun onWithdrawClick() {

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

        if (tokenWithdrawalConfig.customerPin) {
            request.customerPin = binding.customerPinEt.value

            if (request.customerPin.isNullOrEmpty()) {
                indicateError("Please enter a valid customer PIN", binding.customerPinEt)
                return
            }

            if (request.customerPin?.length != 4) {
                indicateError("Customer PIN must be 4 digits", binding.customerPinEt)
                return
            }
        } else {
            request.customerPin = "0000"
        }

        val token = binding.tokenEt.value

        if (token.length < 5) {
            indicateError("Please enter a valid token", binding.tokenEt)
            return
        }

        request.agentPhoneNumber = localStorage.agent?.phoneNumber
        request.customerAccountNumber = accountInfo.number
        request.amount = amount
        request.institutionCode = localStorage.institutionCode
        request.token = token

        request.additionalInformation = Json.encodeToString(
            WithdrawalRequest.Additional.serializer(),
            WithdrawalRequest.Additional(
                customerPhoneNumber = accountInfo.phoneNumber ?: viewModel.phoneNumber.value,
            )
        )

        dialogProvider.requestPIN("Enter agent PIN") {
            onSubmit { pin ->
                request.agentPin = pin
                mainScope.launch { withdraw() }
            }
        }
    }

    private suspend fun withdraw() {
        dialogProvider.showProgressBar("Processing transaction")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.withdrawal(request)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error, finishOnClose)
        response ?: return dialogProvider.showError(
            "Transaction failed. Please try again later",
            finishOnClose
        )

        if (response.isSuccessful) {
            dialogProvider.showSuccess("The withdrawal was successful", finishOnClose)
        } else {
            dialogProvider.showError(response.responseMessage, finishOnClose)
            binding.withdrawBtn.isClickable = true
        }

        if (Platform.hasPrinter) {
            val receipt = WithdrawalReceipt(this@WithdrawActivity, request, accountInfo)
            receipt.apply {
                isSuccessful = response.isSuccessful
                reason = response.responseMessage
            }

            val printerStatus = printer.print(receipt, "Printing...")
            if (printerStatus !== PrinterStatus.READY) {
                showError(printerStatus.message)
            }
        }
    }
}



