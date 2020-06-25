package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.appzonegroup.app.fasttrack.databinding.ActivityBillpaymentBinding
import com.appzonegroup.app.fasttrack.model.Biller
import com.appzonegroup.app.fasttrack.model.BillerItem
import com.appzonegroup.app.fasttrack.model.CustomerAccount
import com.appzonegroup.app.fasttrack.receipt.BillsPaymentReceipt
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import kotlinx.android.synthetic.main.activity_billpayment.*
import kotlinx.coroutines.launch

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/12/2019.
 * Appzone Ltd
 */

class BillPaymentActivity : BaseActivity() {
    private val binding by contentView<BillPaymentActivity, ActivityBillpaymentBinding>(R.layout.activity_billpayment)

    private var amountIsNeeded: Boolean? = false
    private var fieldOneIsNeeded: Boolean? = false
    private var fieldTwoIsNeeded: Boolean? = false
    private val billerItem: BillerItem by lazy { intent.getSerializableExtra("billeritem") as BillerItem }
    private val customer by lazy { intent.getSerializableExtra("customer") as CustomerAccount }
    private var extras: Bundle? = null
    private val biller by lazy { intent.getSerializableExtra("biller") as Biller }

    private val amountTV by lazy { findViewById<TextView>(R.id.billpayment_amount_tv) }
    private val fieldOneTV by lazy { findViewById<TextView>(R.id.fieldone_tv) }
    private val fieldTwoTV by lazy { findViewById<TextView>(R.id.fieldtwo_tv) }
    private val amountET by lazy { findViewById<EditText>(R.id.billpayment_amount_et) }
    private val fieldOneET by lazy { findViewById<EditText>(R.id.billpayment_fieldone_et) }
    private val fieldTwoET by lazy { findViewById<EditText>(R.id.billpayment_fieldtwo_et) }
    private val categoryIdField by lazy { extras!!.getString("categoryId") }
    private val categoryNameField by lazy { extras!!.getString("categoryName") }

    private val isAirtime by lazy { extras!!.getBoolean("isAirtime", false) }
    private val reference = Misc.getRandomString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding

        title = billerItem.billerItemNameField

        val i = intent
        extras = i.extras

        if (billerItem.amountField <= 0) {
            amountIsNeeded = true
            amountET.clearFocus()
            amountTV.visibility = View.VISIBLE
            amountET.visibility = View.VISIBLE
        } else {
            amountIsNeeded = false
            amountET.setText(billerItem.amountField.toString())
            amountET.isEnabled = false
            amountET.isFocusable = false
        }

        if (biller.customerField1 != "") {
            fieldOneIsNeeded = true
            fieldOneTV.text = biller.customerField1
            fieldOneTV.visibility = View.VISIBLE
            fieldOneET.visibility = View.VISIBLE
        } else {
            fieldOneIsNeeded = false
            fieldOneTV.visibility = View.GONE
            fieldOneET.visibility = View.GONE
        }

        if (biller.customerField2 != "") {
            fieldTwoIsNeeded = true
            fieldTwoTV.text = biller.customerField2
            fieldTwoTV.visibility = View.VISIBLE
            fieldTwoET.visibility = View.VISIBLE
        } else {
            fieldOneIsNeeded = false
            fieldTwoTV.visibility = View.GONE
            fieldTwoET.visibility = View.GONE
        }

        if (isAirtime) {
            binding.nameEt.visibility = View.GONE
            binding.nameTv.visibility = View.GONE
            binding.phoneEt.visibility = View.GONE
            binding.phoneTv.visibility = View.GONE
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    internal fun indicateError(message: String, view: View?) {
        showError(message)
        view?.requestFocus()
    }

    fun makeBillPayment(view: View) {
        if (!isAirtime) {
            binding.nameEt.text.toString().run {
                if (isEmpty()) return showNotification("Customer Name is required")
                if (includesSpecialCharacters() || includesNumbers()) return showNotification("Customer Name is invalid")
            }

            binding.phoneEt.text.toString().run {
                if (isEmpty()) return showNotification("Customer Phone is required")
                if (length != 11) return showNotification("Customer Phone must be 11 digits")
            }
        }

        if (binding.billpaymentAmountEt.text.toString().isEmpty()) {
            return showNotification("Amount is required")
        }

        if (amountIsNeeded!!) {
            if (amountET.text.toString().isEmpty()) {
                return showNotification("Please enter an amount")
            } else {
                billerItem.amountField = Integer.parseInt(amountET.text.toString()).toDouble()
            }
        }

        if (fieldOneIsNeeded!!) {
            if (fieldOneET.text.toString().isEmpty()) {
                return showNotification(fieldOneTV.text.toString() + "should not be empty")
            } else {
                billerItem.customerFieldOneField = fieldOneET.text.toString()
            }
        }

        if (fieldTwoIsNeeded!!) {
            if (fieldTwoET.text.toString().isEmpty()) {
                return showNotification(fieldTwoTV.text.toString() + "should not be empty")
            } else {
                billerItem.customerFieldOneField = fieldTwoET.text.toString()
            }
        }
//
//        if (binding.customerEmailEt.text.toString().isEmpty()) {
//            return showNotification("Customer Email is required")
//        }

        if (binding.customerEmailEt.text.toString().isNotEmpty() && !binding.customerEmailEt.text.toString().isValidEmail()) {
            return showNotification("Customer Email is invalid")
        }

        val paymentRequest = PayBillRequest().apply {
            agentPhoneNumber = localStorage.agentPhone
            institutionCode = localStorage.institutionCode
            customerId = "${fieldOneET.text}"
            merchantBillerIdField = "${billerItem.merchantBillerIdField}"
            billItemID = billerItem.billerItemIdField
            amount = "${amountET.text}"
            billerCategoryID = categoryIdField
            customerEmail = "${customer_email_et.text}"
//                customerPhone = customer.phoneNumber
            accountNumber = localStorage.agentPhone
            billerName = biller.billerNameField
            paymentItemCode = billerItem.paymentCodeField
            paymentItemName = billerItem.billerItemNameField
            billerCategoryName = categoryNameField
            customerName = name_et.text.toString()

            customerPhone = if (isAirtime) {
                fieldOneET.text.toString()
            } else phone_et.text.toString()

            customerDepositSlipNumber = reference
            geolocation = gps.geolocationString
            isRecharge = isAirtime
        }

        requestPIN("Enter Agent Pin") {
            onSubmit { pin ->
                paymentRequest.agentPin = pin

                mainScope.launch {
                    showProgressBar("Processing...")
                    val (response, error) = safeRunIO {
                        creditClubMiddleWareAPI.billsPaymentService.runTransaction(paymentRequest)
                    }
                    hideProgressBar()

                    val finishOnClose: DialogListenerBlock<Nothing> = {
                        onClose {
                            setResult(1)
                            finish()
                        }
                    }

                    if (error != null) return@launch showError(error, finishOnClose)

                    if (response == null) {
                        showError("An error occurred", finishOnClose)
                        return@launch
                    }

                    if (response.isSuccessFul == true) {
                        showSuccess(
                            response.responseMessage ?: "Transaction successful",
                            finishOnClose
                        )
                    } else {
                        showError(
                            response.responseMessage
                                ?: getString(R.string.an_error_occurred_please_try_again_later),
                            finishOnClose
                        )
                    }

                    if (Platform.hasPrinter) {
                        val receipt = BillsPaymentReceipt(
                            this@BillPaymentActivity,
                            paymentRequest
                        ).withResponse(response)

                        printer.printAsync(receipt) { printerStatus ->
                            if (printerStatus != PrinterStatus.READY) showError(printerStatus.message)
                        }
                    }
                }
            }
        }
    }
}
