package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.widget.EditText
import com.appzonegroup.app.fasttrack.databinding.ActivityDepositBinding
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.app.fasttrack.receipt.DepositReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.request.DepositRequest
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.requireAccountInfo
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch


/**
 * Created by oto-obong on 2/27/2017.
 */

class DepositActivity : CustomerBaseActivity() {
    private val binding by contentView<DepositActivity, ActivityDepositBinding>(R.layout.activity_deposit)
    override val functionId = FunctionIds.DEPOSIT

    private val depositRequest by lazy { DepositRequest() }

    override fun onCustomerReady(savedInstanceState: Bundle?) {

        binding.accountsSpinner.isEnabled = false

        binding.accountInfoEt.run {
            setText(accountInfo.accountName)

            val chooseAnotherAccount = {
                requireAccountInfo {
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
            attemptDeposit()
        }
    }

    private fun attemptDeposit() {
        val amount = binding.depositAmountEt.text.toString().trim { it <= ' ' }
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

        val agentPIN = binding.agentPinEt.text.toString()

        if (agentPIN.isEmpty()) {
            indicateError("Please enter your PIN", binding.agentPinEt)
            return
        }

        depositRequest.agentPhoneNumber = localStorage.agentPhone
        depositRequest.institutionCode = localStorage.institutionCode
        depositRequest.agentPin = agentPIN
        depositRequest.customerAccountNumber = accountInfo.number
        depositRequest.amount = amount
        depositRequest.geoLocation = gps.geolocationString

        mainScope.launch {
            showProgressBar("Processing Transaction", "Please wait...")

            val (response) = safeRunIO {
                creditClubMiddleWareAPI.staticService.deposit(this@DepositActivity.depositRequest)
            }

            response ?: return@launch showNetworkError()

            if (response.isSuccessful) {

                showSuccess<Unit>("The deposit was successful") {
                    onClose {
                        finish()
                    }
                }

                LocalStorage.setAgentsPin(depositRequest.agentPin, baseContext)

                if (Platform.hasPrinter) {
                    printer.printAsync(
                        DepositReceipt(
                            this@DepositActivity,
                            this@DepositActivity.depositRequest,
                            accountInfo
                        ).apply {
                            isSuccessful = response.isSuccessful
                            reason = response.responseMessage
                        }
                    ) { printerStatus ->
                        if (printerStatus != PrinterStatus.READY) showError(printerStatus.message)
                    }
                }
            } else {
                showError(response.responseMessage ?: "An error occurred. Please try again later")
                binding.depositBtn.isClickable = true
            }
        }
    }

    override fun indicateError(message: String?, view: EditText?) {
        view?.error = message

        binding.depositBtn.isClickable = true

        view?.requestFocus()
    }
}