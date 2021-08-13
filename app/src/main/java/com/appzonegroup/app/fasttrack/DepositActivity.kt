package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.widget.EditText
import com.appzonegroup.app.fasttrack.databinding.ActivityDepositBinding
import com.appzonegroup.app.fasttrack.receipt.DepositReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.prefs.newTransactionReference
import com.creditclub.core.data.request.DepositRequest
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import kotlinx.coroutines.launch
import java.time.Instant

class DepositActivity : CustomerBaseActivity() {
    private val binding by contentView<DepositActivity, ActivityDepositBinding>(R.layout.activity_deposit)
    override val functionId = FunctionIds.DEPOSIT
    private val staticService: StaticService by retrofitService()
    private val retrievalReferenceNumber by lazy {
        localStorage.newTransactionReference()
    }

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
            geoLocation = gps.geolocationString,
            retrievalReferenceNumber = retrievalReferenceNumber
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

    private suspend fun attemptDeposit(depositRequest: DepositRequest) {
        dialogProvider.showProgressBar("Processing Transaction", "Please wait...")
        val (response, error) = safeRunIO {
            staticService.deposit(depositRequest)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        if (response == null) return showNetworkError(finishOnClose)

        val receipt = DepositReceipt(
            this@DepositActivity,
            depositRequest,
            accountInfo,
            isSuccessful = response.isSuccessful,
            reason = response.responseMessage,
            transactionDate = Instant.now()
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