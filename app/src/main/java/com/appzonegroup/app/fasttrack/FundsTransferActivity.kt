package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.appzonegroup.app.fasttrack.databinding.ActivityFundstransferBinding
import com.appzonegroup.app.fasttrack.receipt.FundsTransferReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.data.response.NameEnquiryResponse
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.finishOnClose
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.ui.dataBinding
import kotlinx.android.synthetic.main.activity_fundstransfer.*
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import java.util.*

class FundsTransferActivity : CreditClubActivity(R.layout.activity_fundstransfer) {
    private val binding by dataBinding<ActivityFundstransferBinding>()

    private var destinationBank: String = ""
    internal var accountNumber: String = ""
    internal var amount: String = "0"
    internal var agentPin: String = ""
    private var banks = emptyList<Bank>()

    private val externalTransactionReference = UUID.randomUUID().toString().substring(0, 8)
    private var nameEnquiryResponse: NameEnquiryResponse? = null

    private var isSameBank = false

    override val functionId = FunctionIds.FUNDS_TRANSFER

    private val fundsTransferRequest: FundsTransferRequest
        get() {
            val fundsTransferRequest = FundsTransferRequest()
            fundsTransferRequest.agentPhoneNumber = localStorage.agentPhone!!
            fundsTransferRequest.institutionCode = localStorage.institutionCode!!
            fundsTransferRequest.agentPin = agentPin
            fundsTransferRequest.authToken = "95C1D8B4-7589-4F70-8F20-473E89FB5F01"
            fundsTransferRequest.beneficiaryAccountNumber = accountNumber
            fundsTransferRequest.amountInNaira = amount.toDouble()

            fundsTransferRequest.isToRelatedCommercialBank = isSameBank
            fundsTransferRequest.externalTransactionReference = externalTransactionReference
            fundsTransferRequest.geoLocation = gps.geolocationString
            fundsTransferRequest.narration = binding.narrationEt.text.toString().trim { it <= ' ' }

            if (!isSameBank) {
                val bank = banks[binding.destinationBankSpinner.selectedItemPosition - 1]
                fundsTransferRequest.beneficiaryInstitutionCode = bank.code!!
            }

            if (nameEnquiryResponse != null) {
                fundsTransferRequest.beneficiaryAccountName =
                    nameEnquiryResponse!!.beneficiaryAccountName
                fundsTransferRequest.beneficiaryBVN = nameEnquiryResponse!!.beneficiaryBVN
                fundsTransferRequest.beneficiaryKYC = nameEnquiryResponse!!.beneficiaryKYC
                fundsTransferRequest.nameEnquirySessionID =
                    nameEnquiryResponse!!.nameEnquirySessionID
            }

            return fundsTransferRequest
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.sameBankTv.text = institutionConfig.name
        binding.transferFundsButton.setOnClickListener {
            mainScope.launch { transferFunds() }
        }
        binding.validateButton.setOnClickListener {
            mainScope.launch { validateAccount() }
        }
    }

    fun onOtherBankClick(view: View) {
        isSameBank = false

        mainScope.launch {
            dialogProvider.showProgressBar("Getting bank information")

            val (banks) = safeRunIO {
                creditClubMiddleWareAPI.fundsTransferService.getBanks(localStorage.institutionCode)
            }

            dialogProvider.hideProgressBar()

            banks ?: return@launch showNetworkError()

            this@FundsTransferActivity.banks = banks

            val bankNames = ArrayList<String>()
            bankNames.add("Select bank...")

            for (bank in banks) {
                bankNames.add(bank.name ?: "Unknown")
            }

            val spinnerArrayAdapter = ArrayAdapter(
                this@FundsTransferActivity,
                android.R.layout.simple_spinner_item,
                bankNames
            )
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.destinationBankSpinner.adapter = spinnerArrayAdapter

            transfer_type_layout.visibility = View.GONE
            bank_details_layout.visibility = View.VISIBLE
        }
    }

    fun onSameBankClick(view: View) {
        isSameBank = true

        transfer_type_layout.visibility = View.GONE
        bank_details_layout.visibility = View.VISIBLE
        destination_bank_spinner.visibility = View.GONE
        destination_bank_tv.visibility = View.GONE
    }

    private fun indicateError(message: String, view: View?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        view?.requestFocus()
    }

    private suspend fun transferFunds() {
        agentPin = binding.agentPinEt.text.toString()
        if (agentPin.isEmpty()) {
            indicateError("Please enter your PIN", binding.agentPinEt as View)
            return
        }

        amount = binding.amountEt.text.toString().trim { it <= ' ' }
        if (amount === "") {
            indicateError("Please enter an Amount", binding.amountEt as View)
            return
        }

        val amountDouble: Double

        try {
            amountDouble = java.lang.Double.parseDouble(amount)
        } catch (ex: Exception) {
            indicateError("Please enter a valid amount", binding.amountEt as View)
            return
        }

        dialogProvider.showProgressBar("Transfer in progress")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.fundsTransferService.transfer(fundsTransferRequest)
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
            (amountDouble * 100).toLong(),
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
        findViewById<View>(R.id.bank_details_layout).visibility =
            if (isForFinalAction) View.GONE else View.VISIBLE
        findViewById<View>(R.id.other_details_layout).visibility =
            if (isForFinalAction) View.VISIBLE else View.GONE
    }

    fun backClicked(view: View) {
        showControls(false)
    }

    private suspend fun validateAccount() {
        if (!isSameBank) {
            destinationBank = binding.destinationBankSpinner.selectedItem.toString()
            if (binding.destinationBankSpinner.selectedItemPosition == 0) {
                indicateError("No Bank was selected", binding.destinationBankSpinner)
                return
            }
        }

        accountNumber = binding.accountNumberEt.text.toString()
        if (accountNumber.length != 10 && accountNumber.length != 11) {
            indicateError(
                "Please enter a valid Account number",
                binding.accountNumberEt as View
            )
            return
        }

        dialogProvider.showProgressBar("Validating account information")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.fundsTransferService.nameEnquiry(fundsTransferRequest)
        }
        nameEnquiryResponse = response

        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showError(error)
            return
        }

        response ?: return dialogProvider.showError("Please enter a valid account number")

        if (response.status) {
            binding.accountNameEt.setText(response.beneficiaryAccountName)
            showControls(true)
        } else {
            response.responseMessage ?: return showInternalError()
            dialogProvider.showError(response.responseMessage)
        }
    }
}
