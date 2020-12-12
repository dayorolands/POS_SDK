package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.appzonegroup.app.fasttrack.databinding.ActivityFundstransferBinding
import com.appzonegroup.app.fasttrack.fragment.FundsTransferViewModel
import com.appzonegroup.app.fasttrack.receipt.FundsTransferReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.*
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.ui.dataBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import java.util.*

class FundsTransferActivity : CreditClubActivity(R.layout.activity_fundstransfer) {
    private val binding by dataBinding<ActivityFundstransferBinding>()
    private val fundsTransferService = creditClubMiddleWareAPI.fundsTransferService
    private val viewModel: FundsTransferViewModel by viewModels()

    private val transactionReference = UUID.randomUUID().toString().substring(0, 8)

    override val functionId = FunctionIds.FUNDS_TRANSFER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewModel = viewModel
        binding.sameBankTv.text = institutionConfig.name
        binding.transferFundsButton.setOnClickListener {
            mainScope.launch { transferFunds() }
        }
        binding.validateButton.setOnClickListener {
            mainScope.launch { validateAccount() }
        }
    }

    fun onOtherBankClick(view: View) {
        viewModel.isSameBank.value = false

        mainScope.launch {
            dialogProvider.showProgressBar("Getting bank information")
            val (banks, error) = safeRunIO {
                fundsTransferService.getBanks(localStorage.institutionCode)
            }
            dialogProvider.hideProgressBar()

            if (error != null) return@launch dialogProvider.showError(error)
            banks ?: return@launch showNetworkError()
            viewModel.bankList.value = banks

            binding.transferTypeLayout.visibility = View.GONE
            binding.bankDetailsLayout.visibility = View.VISIBLE
        }
    }

    fun onSameBankClick(view: View) {
        viewModel.isSameBank.value = true

        binding.transferTypeLayout.visibility = View.GONE
        binding.bankDetailsLayout.visibility = View.VISIBLE
    }

    private fun indicateError(message: String, view: View?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        view?.requestFocus()
    }

    private suspend fun transferFunds() {
        val pin = dialogProvider.getPin("Agent PIN") ?: return
        if (pin.isEmpty()) return showError("Please enter your PIN")
        if (pin.length != 4) return showError("PIN must be four digits")

        val amount = viewModel.amountString.value?.trim { it <= ' ' }
        if (amount === "") {
            indicateError("Please enter an Amount", binding.amountEt as View)
            return
        }

        val amountDouble = amount?.toDoubleOrNull()
        if (amountDouble == null) {
            indicateError("Please enter a valid amount", binding.amountEt as View)
            return
        }

        val fundsTransferRequest = FundsTransferRequest().apply {
            agentPhoneNumber = localStorage.agentPhone
            institutionCode = localStorage.institutionCode
            agentPin = pin
            authToken = "95C1D8B4-7589-4F70-8F20-473E89FB5F01"
            beneficiaryAccountNumber = viewModel.receiverAccountNumber.value
            amountInNaira = amount.toDouble()

            isToRelatedCommercialBank = viewModel.isSameBank.value ?: false
            externalTransactionReference = transactionReference
            geoLocation = gps.geolocationString
            narration = viewModel.narration.value?.trim { it <= ' ' }

            if (viewModel.isSameBank.value != true) {
                beneficiaryInstitutionCode = viewModel.bank.value?.code
            }
        }

        viewModel.nameEnquiryResponse.value?.run {
            fundsTransferRequest.beneficiaryAccountName = beneficiaryAccountName
            fundsTransferRequest.beneficiaryBVN = beneficiaryBVN
            fundsTransferRequest.beneficiaryKYC = beneficiaryKYC
            fundsTransferRequest.nameEnquirySessionID = nameEnquirySessionID
        }

        dialogProvider.showProgressBar("Transfer in progress")
        val (response, error) = safeRunIO {
            fundsTransferService.transfer(fundsTransferRequest)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error, finishOnClose)
        response ?: return showNetworkError(finishOnClose)

        val receipt = FundsTransferReceipt(
            this@FundsTransferActivity,
            fundsTransferRequest
        ).apply {
            isSuccessful = response.isSuccessful
            reason = response.responseMessage
        }

        renderTransactionStatusPage(
            getString(R.string.fund_stransfer),
            amountDouble.toLong(),
            response.isSuccessful,
            response.responseMessage,
            receipt
        )

        if (Platform.hasPrinter) {
            val printer = get<PosPrinter> { parametersOf(this, dialogProvider) }
            printer.printAsync(receipt) { printerStatus ->
                if (printerStatus != PrinterStatus.READY) dialogProvider.showError(printerStatus.message)
            }
        }
    }

    private fun showControls(isForFinalAction: Boolean) {
        binding.bankDetailsLayout.visibility = if (isForFinalAction) View.GONE else View.VISIBLE
        binding.otherDetailsLayout.visibility = if (isForFinalAction) View.VISIBLE else View.GONE
    }

    fun backClicked(view: View) {
        showControls(false)
    }

    private suspend fun validateAccount() {
        if (viewModel.isSameBank.value != true && viewModel.bank.value == null) {
            return dialogProvider.showError("No Bank was selected")
        }

        val accountNumber = viewModel.receiverAccountNumber.value
        if (accountNumber.isNullOrBlank() || accountNumber.length != 10 && accountNumber.length != 11) {
            indicateError(
                "Please enter a valid Account number",
                binding.receiverAccountNumberEt as View
            )
            return
        }

        val nameEnquiryRequest = FundsTransferRequest().apply {
            agentPhoneNumber = localStorage.agentPhone
            institutionCode = localStorage.institutionCode
            authToken = "95C1D8B4-7589-4F70-8F20-473E89FB5F01"

            isToRelatedCommercialBank = viewModel.isSameBank.value == true
            externalTransactionReference = transactionReference
            geoLocation = gps.geolocationString
            beneficiaryAccountNumber = viewModel.receiverAccountNumber.value

            if (viewModel.isSameBank.value != true) {
                beneficiaryInstitutionCode = viewModel.bank.value?.code
            }
        }

        dialogProvider.showProgressBar("Validating account information")
        val (response, error) = safeRunIO {
            fundsTransferService.nameEnquiry(nameEnquiryRequest)
        }
        dialogProvider.hideProgressBar()

        viewModel.nameEnquiryResponse.value = response

        if (error != null && (error is SerializationException || error.isKotlinNPE())) {
            return dialogProvider.showError("Invalid account number")
        }
        if (error != null) return dialogProvider.showError(error)

        response ?: return dialogProvider.showError("Invalid account number")

        if (response.status) {
            showControls(true)
        } else {
            dialogProvider.showError(response.responseMessage ?: "Invalid account number")
        }
    }
}
