package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import com.appzonegroup.app.fasttrack.databinding.ActivityWithdrawBinding
import com.appzonegroup.app.fasttrack.receipt.WithdrawalReceipt
import com.appzonegroup.app.fasttrack.ui.EditText
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.creditclub.core.contract.FormDataHolder
import com.creditclub.core.data.request.WithdrawalRequest
import com.creditclub.core.type.TokenType
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.generateRRN
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.sendToken
import kotlinx.coroutines.launch

/**
 * Created by DELL on 2/27/2017.
 */

class WithdrawActivity : CustomerBaseActivity(), FormDataHolder<WithdrawalRequest> {

    private val binding by contentView<WithdrawActivity, ActivityWithdrawBinding>(
        R.layout.activity_withdraw
    )
    override val functionId = FunctionIds.TOKEN_WITHDRAWAL

    override val formData = WithdrawalRequest().apply {
        retrievalReferenceNumber = generateRRN()
    }

    private var tokenSent: Boolean = false

    private var amount: String = ""

    override fun onCustomerReady(savedInstanceState: Bundle?) {
        binding.accountsSpinner.isEnabled = false

        if (!gps.canGetLocation()) {
            gps.showSettingsAlert()
        }

        val accountInfoEt = findViewById<EditText>(R.id.account_info_et)

        val chooseAnotherAccount = {
            javaRequireAccountInfo {
                onSubmit { accountInfo ->
                    this@WithdrawActivity.accountInfo = accountInfo
                    accountInfoEt.setText(accountInfo.accountName)
                }
            }
        }

        accountInfoEt.run {
            setText(accountInfo.accountName)
            setOnFocusChangeListener { _, hasFocus -> if (hasFocus) chooseAnotherAccount() }
            setOnClickListener { if (accountInfoEt.isFocused) chooseAnotherAccount() }
        }

        if (institutionConfig.flows.tokenWithdrawal.customerPin) {
            binding.customerPinEt.visibility = View.VISIBLE
        }
    }

    fun send_token_clicked(view: View) {
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
                this@WithdrawActivity.run {
                    findViewById<View>(R.id.withdrawal_amount_et).isEnabled = false
                    findViewById<View>(R.id.top_layout).isEnabled = false
                    findViewById<View>(R.id.send_token_btn).visibility = View.GONE
                    findViewById<View>(R.id.token_block).visibility = View.VISIBLE
                }
            }
        }
    }

    fun withdraw_button_click(view: View) {

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

        if (institutionConfig.flows.tokenWithdrawal.customerPin) {
            formData.customerPin = binding.customerPinEt.value

            if (formData.customerPin.isNullOrEmpty()) {
                indicateError("Please enter a valid customer PIN", binding.customerPinEt)
                return
            }

            if (formData.customerPin?.length != 4) {
                indicateError("Customer PIN must be 4 digits", binding.customerPinEt)
                return
            }
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

        requestPIN("Enter agent PIN") {
            onSubmit { pin ->
                formData.agentPin = pin
                withdraw()
            }
        }
    }

    fun withdraw() {

        mainScope.launch {

            showProgressBar("Processing transaction")
            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.staticService.withdrawal(formData)
            }
            hideProgressBar()

            if (error != null) return@launch showError(error)
            response ?: return@launch showError("Transaction failed. Please try again later")

            if (response.isSuccessful) {

                showSuccess<Nothing>("The withdrawal was successful") {
                    onClose {
                        finish()
                    }
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
            } else {
                showError(response.responseMessage)
                findViewById<View>(R.id.withdraw_btn).isClickable = true
            }
        }
    }
}



