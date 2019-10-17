package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.app.fasttrack.databinding.ActivityWithdrawBinding
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.WithdrawalRequest
import com.appzonegroup.app.fasttrack.receipt.WithdrawalReceipt
import com.appzonegroup.app.fasttrack.ui.EditText
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.task.PostCallTask
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.creditclub.core.type.TokenType
import com.creditclub.core.util.generateRRN
import com.creditclub.core.util.sendToken
import com.google.gson.Gson

/**
 * Created by DELL on 2/27/2017.
 */

class WithdrawActivity : CustomerBaseActivity() {

    private var binding: ActivityWithdrawBinding? = null

    internal var agentPhone = ""
    internal var institutionCode = ""
    internal var gson: Gson = Gson()
    internal var wtReq = WithdrawalRequest().apply {
        retrievalReferenceNumber = generateRRN()
    }

    private var tokenSent: Boolean = false

    internal var internetAction: InternetAction = InternetAction.SendToken

    internal var amount: String = "0"

    internal enum class InternetAction {
        SendToken,
        GetAccount,
        Withdraw
    }

    override fun onCustomerReady(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw)

        binding!!.accountsSpinner.isEnabled = false
        gson = Gson()

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

        accountInfoEt.setText(accountInfo.accountName)

        accountInfoEt.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) chooseAnotherAccount() }

        accountInfoEt.setOnClickListener { v -> if (accountInfoEt.isFocused) chooseAnotherAccount() }
    }

    fun send_token_clicked(view: View) {
        amount = binding!!.withdrawalAmountEt.text!!.toString().trim { it <= ' ' }


        if (amount.isEmpty()) {
            showError(getString(R.string.please_enter_an_amount))
            return
        }

        try {
            java.lang.Double.parseDouble(amount)
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

        amount = binding!!.withdrawalAmountEt.text!!.toString().trim { it <= ' ' }
        if (amount.isEmpty()) {
            indicateError("Enter an amount", binding!!.withdrawalAmountEt)
            return
        }

        try {
            java.lang.Double.parseDouble(amount)
        } catch (ex: Exception) {
            indicateError("Please enter a valid amount", binding!!.withdrawalAmountEt)
            return
        }

        val token = binding!!.tokenEt.text!!.toString().trim { it <= ' ' }
        if (token.length != 5) {
            indicateError("Please enter the customer's token", binding!!.tokenEt)
            return
        }

        wtReq.agentPhoneNumber = LocalStorage.getPhoneNumber(baseContext)
        wtReq.agentPin = LocalStorage.getAgentsPin(baseContext)
        wtReq.customerAccountNumber = accountInfo.number
        wtReq.amount = amount
        wtReq.institutionCode = LocalStorage.getInstitutionCode(baseContext)
        wtReq.token = token

        internetAction = InternetAction.Withdraw
        showProgressBar("Processing transaction")
        PostCallTask(
            progressDialog,
            this,
            this
        ).execute(
            AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/WithDrawal",
            Gson().toJson(wtReq)
        )
    }

    private fun processWithdrawalResponse(result: String?) {
        var result = result
        if (result != null) {
            result = result.replace("\\", "").replace("\n", "").trim { it <= ' ' }
            val response =
                Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response::class.java)
            if (response.isSuccessful) {
                //showNotification("Sucessfull Deposit");

                val dialog =
                    Dialogs.getSuccessDialog(this@WithdrawActivity, "The withdrawal was successful")
                dialog.findViewById<View>(R.id.close_btn).setOnClickListener { view ->
                    dialog.dismiss()
                    finish()
                }
                dialog.show()

                //emptyInputs();
                LocalStorage.setAgentsPin(LocalStorage.getAgentsPin(baseContext), baseContext)
                //depositButton.setClickable(true);

                if (Platform.hasPrinter) {
                    printer.printAsync(
                        WithdrawalReceipt(this, wtReq, accountInfo).apply {
                            isSuccessful = response.isSuccessful
                            reason = response.reponseMessage
                        },
                        "Printing..."
                    ) { printerStatus ->
                        if (printerStatus !== PrinterStatus.READY) {
                            showError(printerStatus.message)
                        }
                        null
                    }
                }
            } else {
                showError(response.reponseMessage)
                findViewById<View>(R.id.withdraw_btn).isClickable = true
            }
        } else {
            showError("A network-related error just occurred. Please check your internet connection and try again")
        }
    }

    override fun processFinished(output: String?) {

        if (internetAction == InternetAction.Withdraw) {
            processWithdrawalResponse(output)
        }
    }
}



