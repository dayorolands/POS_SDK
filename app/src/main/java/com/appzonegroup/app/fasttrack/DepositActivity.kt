package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.widget.EditText
import com.appzonegroup.app.fasttrack.databinding.ActivityDepositBinding
import com.appzonegroup.app.fasttrack.receipt.DepositReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.request.DepositRequest
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import com.creditclub.pos.printer.PrinterStatus
import kotlinx.coroutines.launch
import java.time.Instant


/**
 * Created by oto-obong on 2/27/2017.
 */

class DepositActivity : CustomerBaseActivity() {
    private val binding by contentView<DepositActivity, ActivityDepositBinding>(R.layout.activity_deposit)
    override val functionId = FunctionIds.DEPOSIT
    private val staticService: StaticService by retrofitService()

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

        val agentPIN = binding.agentPinEt.value

        if (agentPIN.isEmpty()) {
            indicateError("Please enter your PIN", binding.agentPinEt)
            return
        }

        val depositRequest = DepositRequest(
            agentPhoneNumber = localStorage.agentPhone,
            institutionCode = localStorage.institutionCode,
            agentPin = agentPIN,
            customerAccountNumber = accountInfo.number,
            amount = amount,
            geoLocation = gps.geolocationString,
        )
        renderTransactionSummary(
            amount = amount.toDouble(),
            onProceed = {
                attemptDeposit(depositRequest)
            },
            fetchFeeAgent = {
                staticService.getDepositFee(request = depositRequest)
            }
        )
    }

    private suspend fun attemptDeposit(depositRequest: DepositRequest) {
        dialogProvider.showProgressBar("Processing Transaction", "Please wait...")
        val (response) = safeRunIO {
            staticService.deposit(depositRequest)
        }
        dialogProvider.hideProgressBar()

        if (response == null) return showNetworkError(finishOnClose)
        if (response.isSuccessful) {
            dialogProvider.showSuccess("The deposit was successful", finishOnClose)
        } else {
            val message = response.responseMessage ?: "An error occurred. Please try again later"
            dialogProvider.showError(message, finishOnClose)
        }

        if (Platform.hasPrinter) {
            val printerStatus = printer.print(
                DepositReceipt(
                    this@DepositActivity,
                    depositRequest,
                    accountInfo,
                    isSuccessful = response.isSuccessful,
                    reason = response.responseMessage,
                    transactionDate = Instant.now()
                        .toString(CREDIT_CLUB_REQUEST_DATE_PATTERN)
                        .replace("T", " "),
                )
            )
            if (printerStatus != PrinterStatus.READY) {
                dialogProvider.showError(printerStatus.message)
            }
        }
    }

    override fun indicateError(message: String?, view: EditText?) {
        view?.error = message
        view?.requestFocus()
    }
}