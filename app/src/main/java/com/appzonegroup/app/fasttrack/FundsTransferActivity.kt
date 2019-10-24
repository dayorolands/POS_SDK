package com.appzonegroup.app.fasttrack

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.app.fasttrack.receipt.FundsTransferReceipt
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.data.response.NameEnquiryResponse
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_fundstransfer.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Oto-obong on 13/7/2017.
 */

class FundsTransferActivity : BaseActivity() {

    internal val destinationBankSpinner: Spinner by lazy { findViewById<View>(R.id.spinner_destination_bank) as Spinner }
    internal val destinationAccountNumber_et: EditText by lazy { findViewById<View>(R.id.fundstransfer_accountnumber) as EditText }
    internal val accountName_et: EditText by lazy { findViewById<View>(R.id.fundstransfer_accountname) as EditText }
    internal val amount_et: EditText by lazy { findViewById<View>(R.id.fundstransfer_amount) as EditText }
    internal val agentPin_et: EditText by lazy { findViewById<View>(R.id.fundstransfer_agentpin) as EditText }
    internal val narrationEt: EditText by lazy { findViewById(R.id.fundstransfer_narration_et) as EditText }

    internal var destinationBank: String = ""
    internal var accountNumber: String = ""
    internal var amount: String = "0"
    internal var agentPin: String = ""
    internal var gson: Gson = Gson()
    internal var banks = emptyList<Bank>()
    internal val backgroundHandler: Handler by lazy { Misc.setupScheduler() }
    internal val locationManager: LocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    internal var nameEnquiryResponse: NameEnquiryResponse? = null

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
            fundsTransferRequest.externalTransactionReference =
                UUID.randomUUID().toString().substring(0, 8)
            fundsTransferRequest.geoLocation = gps.geolocationString
            fundsTransferRequest.narration = narrationEt.text.toString().trim { it <= ' ' }

            if (!isSameBank) {
                val bank = banks[destinationBankSpinner.selectedItemPosition - 1]
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

    private val receipt by lazy {
        FundsTransferReceipt(
            this,
            fundsTransferRequest
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fundstransfer)

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Call your Alert message
            val errorDialog = Dialogs.getErrorDialog(
                this,
                "Your GPS is not on. Please switch it on, shake your ic_phone and try again."
            )
            errorDialog.findViewById<View>(R.id.close_btn).setOnClickListener {
                errorDialog.dismiss()
                finish()
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return
        }

        hideSoftKeyboard()
    }

    override fun showNotification(message: String) {
        Dialogs.showErrorMessage(this, message)
        // Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    fun onOtherBankClick(view: View) {
        isSameBank = false

        mainScope.launch {
            showProgressBar("Getting bank information")

            val (banks) = safeRunIO {
                creditClubMiddleWareAPI.fundsTransferService.getBanks(localStorage.institutionCode)
            }

            hideProgressBar()

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
            destinationBankSpinner.adapter = spinnerArrayAdapter

            transfer_type_layout.visibility = View.GONE
            bank_details_layout.visibility = View.VISIBLE
        }
    }

    fun onSameBankClick(view: View) {
        isSameBank = true

        transfer_type_layout.visibility = View.GONE
        bank_details_layout.visibility = View.VISIBLE
        spinner_destination_bank.visibility = View.GONE
        destination_bank_tv.visibility = View.GONE
    }

    internal fun indicateError(message: String, view: View?) {

        showNotification(message)
        view?.requestFocus()
    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    fun transferFunds(view: View) {

        agentPin = agentPin_et.text.toString()
        if (agentPin.isEmpty()) {
            indicateError("Please enter your PIN", agentPin_et as View)
            return
        }

        amount = amount_et.text.toString().trim { it <= ' ' }
        if (amount === "") {
            indicateError("Please enter an Amount", amount_et as View)
            return
        }

        val amountDouble: Double

        try {
            amountDouble = java.lang.Double.parseDouble(amount!!)
        } catch (ex: Exception) {
            indicateError("Please enter a valid amount", amount_et as View)
            return
        }

        /*if (gpsLocation == null)
        {
            showError("Please switch on your ic_phone GPS, shake your ic_phone and try again.");
            return;
        }*/
        /*gson = new Gson();
        FundsTransferRequest transferData = new FundsTransferRequest();
        transferData.setAgentPhoneNumber(LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext()));
        transferData.setAgentPin(agentPin);
        transferData.setAuthToken("681B3AEA-835E-456D-ADBB-8C4C6D9CBADC");
        transferData.setBeneficiaryAccountNumber(accountNumber);
        transferData.setAmountInNaira(amountDouble);
        Bank bank = banks.get(destinationBankSpinner.getSelectedItemPosition() - 1);
        transferData.setBeneficiaryInstitutionCode(bank.getBankCode());
        transferData.setToRelatedCommercialBank(bank.getBankCode().equals("000001"));
        transferData.setExternalTransactionReference(UUID.randomUUID().toString().substring(0, 8));
        transferData.setGeoLocation(gpsLocation);*/

        mainScope.launch {
            showProgressBar("Transfer in progress")
            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.fundsTransferService.transfer(fundsTransferRequest)
            }
            hideProgressBar()

            if (error != null) return@launch showError(error)
            response ?: return@launch showNetworkError()

            if (response.isSuccessful) {
                showSuccess<Nothing>("Transfer was successful") {
                    onClose {
                        finish()
                    }
                }
            } else {
                showError(response.responseMessage)
            }

            if (Platform.hasPrinter) {
                receipt.apply {
                    isSuccessful = response.isSuccessful
                    reason = response.responseMessage
                }

                printer.printAsync(receipt) { printerStatus ->
                    if (printerStatus != PrinterStatus.READY) showError(printerStatus.message)
                }
            }
        }
    }

    internal fun showControls(isForFinalAction: Boolean) {
        findViewById<View>(R.id.bank_details_layout).visibility =
            if (isForFinalAction) View.GONE else View.VISIBLE
        findViewById<View>(R.id.other_details_layout).visibility =
            if (isForFinalAction) View.VISIBLE else View.GONE
    }

    fun backClicked(view: View) {
        showControls(false)
    }

    fun validateClicked(view: View) {
        if (!isSameBank) {
            destinationBank = destinationBankSpinner.selectedItem.toString()
            if (destinationBankSpinner.selectedItemPosition == 0) {
                indicateError("No Bank was selected", destinationBankSpinner)
                return
            }
        }

        accountNumber = destinationAccountNumber_et.text.toString()
        if (accountNumber.length != 10 && accountNumber.length != 11) {
            indicateError(
                "Please enter a valid Account number",
                destinationAccountNumber_et as View
            )
            return
        }

        mainScope.launch {
            showProgressBar("Validating account information")

            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.fundsTransferService.nameEnquiry(fundsTransferRequest)
            }
            nameEnquiryResponse = response

            hideProgressBar()

            if (error != null) {
                showError(error)
                return@launch
            }

            response ?: return@launch showError("Please enter a valid account number")

            if (response.status) {
                (findViewById<View>(R.id.fundstransfer_accountname) as EditText).setText(
                    response.beneficiaryAccountName
                )
                showControls(true)
            } else {
                response.responseMessage ?: return@launch showInternalError()
                showError(response.responseMessage)
            }
        }
    }
}
