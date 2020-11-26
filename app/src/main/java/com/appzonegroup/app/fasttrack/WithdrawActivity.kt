package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import com.appzonegroup.app.fasttrack.databinding.ActivityWithdrawBinding
import com.appzonegroup.app.fasttrack.receipt.WithdrawalReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.core.contract.FormDataHolder
import com.creditclub.core.data.request.WithdrawalRequest
import com.creditclub.core.type.TokenType
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Created by DELL on 2/27/2017.
 */

class WithdrawActivity : CustomerBaseActivity(), FormDataHolder<WithdrawalRequest> {

    private val binding by contentView<WithdrawActivity, ActivityWithdrawBinding>(
        R.layout.activity_withdraw
    )
    override val functionId = FunctionIds.TOKEN_WITHDRAWAL
    private val tokenWithdrawalConfig by lazy { institutionConfig.flows.tokenWithdrawal }

    override val formData = WithdrawalRequest().apply {
        retrievalReferenceNumber = generateRRN()
    }

    private var tokenSent: Boolean = false

    private var amount: String = ""

    override fun onCustomerReady(savedInstanceState: Bundle?) {
        binding.accountsSpinner.isEnabled = false

        if (tokenWithdrawalConfig.externalToken) {
            tokenSent = true
            binding.withdrawalAmountEt.isEnabled = true
            binding.topLayout.isEnabled = false
            binding.sendTokenBtn.visibility = View.GONE
            binding.tokenBlock.visibility = View.VISIBLE
        }

        if (!gps.canGetLocation()) {
            gps.showSettingsAlert()
        }

        val chooseAnotherAccount = {
            requireAccountInfo(options = customerRequestOptions) {
                onSubmit { accountInfo ->
                    this@WithdrawActivity.accountInfo = accountInfo
                    binding.accountInfoEt.setText(accountInfo.accountName)
                }
            }
        }

        binding.accountInfoEt.run {
            setText(accountInfo.accountName)
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) chooseAnotherAccount() }
            setOnClickListener { if (isFocused) chooseAnotherAccount() }
        }

        binding.sendTokenBtn.setOnClickListener {
            mainScope.launch { sendToken() }
        }

        binding.withdrawBtn.setOnClickListener {
            mainScope.launch { onWithdrawClick() }
        }

        if (tokenWithdrawalConfig.customerPin) {
            binding.customerPinEt.visibility = View.VISIBLE
        }
    }

    private fun sendToken() {
        amount = binding.withdrawalAmountEt.text!!.toString().trim { it <= ' ' }

        if (amount.isEmpty()) {
            showError(getString(R.string.please_enter_an_amount))
            return
        }

        try {
            amount.toDouble()
        } catch (ex: Exception) {
            showError(getString(R.string.please_enter_a_valid_amount))
            return
        }

        sendToken(accountInfo, TokenType.Withdrawal, amount.toDouble()) {
            onSubmit {
                tokenSent = true
                binding.withdrawalAmountEt.isEnabled = false
                binding.topLayout.isEnabled = false
                binding.sendTokenBtn.visibility = View.GONE
                binding.tokenBlock.visibility = View.VISIBLE
            }
        }
    }

    private fun onWithdrawClick() {

        if (!tokenSent) {
            showError("No token has been sent to the customer. Click the \"Send Token\" button to continue")
            return
        }

        amount = binding.withdrawalAmountEt.value

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

        if (tokenWithdrawalConfig.customerPin) {
            formData.customerPin = binding.customerPinEt.value

            if (formData.customerPin.isNullOrEmpty()) {
                indicateError("Please enter a valid customer PIN", binding.customerPinEt)
                return
            }

            if (formData.customerPin?.length != 4) {
                indicateError("Customer PIN must be 4 digits", binding.customerPinEt)
                return
            }
        } else {
            formData.customerPin = "0000"
        }

        val token = binding.tokenEt.value

        if (token.length != 5) {
            indicateError("Please enter the customer's token", binding.tokenEt)
            return
        }

        formData.agentPhoneNumber = localStorage.agent?.phoneNumber
        formData.customerAccountNumber = accountInfo.number
        formData.amount = amount
        formData.institutionCode = localStorage.institutionCode
        formData.token = token

        val additionalInformation = WithdrawalRequest.Additional().apply {
            customerPhoneNumber = accountInfo.phoneNumber
        }
        formData.additionalInformation = Json.encodeToString(
            WithdrawalRequest.Additional.serializer(),
            additionalInformation
        )

        dialogProvider.requestPIN("Enter agent PIN") {
            onSubmit { pin ->
                formData.agentPin = pin
                mainScope.launch { withdraw() }
            }
        }
    }

    private suspend fun withdraw() {
        showProgressBar("Processing transaction")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.withdrawal(formData)
        }
        hideProgressBar()

        if (error != null) return showError(error, finishOnClose)
        response ?: return showError(
            "Transaction failed. Please try again later",
            finishOnClose
        )

        if (response.isSuccessful) {
            showSuccess("The withdrawal was successful", finishOnClose)
        } else {
            showError(response.responseMessage, finishOnClose)
            binding.withdrawBtn.isClickable = true
        }

        if (Platform.hasPrinter) {
            val receipt = WithdrawalReceipt(this@WithdrawActivity, formData, accountInfo)
            receipt.apply {
                isSuccessful = response.isSuccessful
                reason = response.responseMessage
            }

            printer.printAsync(receipt, "Printing...") { printerStatus ->
                if (printerStatus !== PrinterStatus.READY) {
                    showError(printerStatus.message)
                }
            }
        }
    }
}



