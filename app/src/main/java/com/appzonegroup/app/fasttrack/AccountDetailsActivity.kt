package com.appzonegroup.app.fasttrack


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.Balance
import com.appzonegroup.app.fasttrack.model.BalanceEnquiry
import com.appzonegroup.app.fasttrack.network.ApiServiceObject
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.google.gson.Gson

class AccountDetailsActivity : BaseActivity() {

    private val accountNumberEt by lazy { findViewById<EditText>(R.id.bal_customer_account_number_et) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers_details)
    }

    fun getCustomersBal(v: View) {
        val customerAccountNumber = accountNumberEt.text.toString().trim { it <= ' ' }

        if (customerAccountNumber.isEmpty()) {
            indicateError("Please enter the customer's account number", accountNumberEt)
            return
        }

        try {
            java.lang.Long.parseLong(customerAccountNumber)
        } catch (ex: Exception) {
            indicateError("Please enter a valid account number", accountNumberEt)
            return
        }

        if (customerAccountNumber.length != 10) {
            indicateError("Please enter a complete account number", accountNumberEt)
            return
        }

        showProgressBar("Processing")

        val Token = LocalStorage.GetValueFor(AppConstants.API_TOKEN, baseContext)
        val phoneNumber = LocalStorage.getPhoneNumber(baseContext)

        val urlString = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/BalanceEnquiry"
        val balanceEnquiry = BalanceEnquiry()
        balanceEnquiry.agentPin = LocalStorage.getAgentsPin(baseContext)
        balanceEnquiry.customerAccountNumber = customerAccountNumber
        balanceEnquiry.agentPhoneNumber = LocalStorage.getPhoneNumber(baseContext)
        balanceEnquiry.institutionCode = LocalStorage.getInstitutionCode(baseContext)

        ApiServiceObject.postAsync(urlString, balanceEnquiry) { (result, error) ->
            hideProgressBar()
            if (error != null) return@postAsync showError(error.message ?: "")

            result ?: showError("A network-related error just occurred. Please try again later")

            Log.e("Balance", result + "")

            catchError {
                val response = Gson().fromJson(result, Balance::class.java)
                if (response.isSussessful) {
                    showSuccess(response.responseMessage)
                } else {
                    showError(response.responseMessage)
                }
                /*}
                    else {
                        com.appzonegroup.app.fasttrack.model.Response response = new Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response.class);
                        showError(response.getReponseMessage());
                    }*/
            }
        }
    }
}
