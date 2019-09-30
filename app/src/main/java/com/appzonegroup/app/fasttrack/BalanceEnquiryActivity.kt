package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.appzonegroup.app.fasttrack.ui.TextView
import com.appzonegroup.app.fasttrack.utility.Misc
import com.creditclub.core.data.request.BalanceEnquiryRequest
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import kotlinx.android.synthetic.main.activity_balance_enquiry.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Oto-obong on 11/10/2017.
 */

class BalanceEnquiryActivity : BaseActivity() {

    lateinit var available_balance: TextView
    lateinit var balance: TextView
    internal lateinit var pin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance_enquiry)
        balance = findViewById<View>(R.id.balance) as TextView
        available_balance = findViewById<View>(R.id.available_balance) as TextView
    }


    fun onClick(view: View) {
        pin = (findViewById<View>(R.id.pin_et) as EditText).text.toString().trim { it <= ' ' }
        if (pin.length != 4) {
            showError("Please enter your PIN")
            return
        }

        getAgentsBalance(pin)
    }

    private fun getAgentsBalance(pin: String) {
        mainScope.launch {

            val request = BalanceEnquiryRequest().apply {
                agentPin = pin
                agentPhoneNumber = localStorage.agentPhone
                institutionCode = localStorage.institutionCode
            }

            showProgressBar("Getting Balance")
            val (response) = safeRunIO {
                creditClubMiddleWareAPI.staticService.balanceEnquiry(request)
            }
            hideProgressBar()

            response ?: return@launch showNetworkError()

            if (response.isSussessful) {
                val balanceOut = String.format(Locale.getDefault(), "%s %s", "₦", Misc.toMoneyFormat(response.balance))
                val availableOut =
                    String.format(Locale.getDefault(), "%s %s", "₦", Misc.toMoneyFormat(response.availableBalance))

                localStorage.agentPIN = pin

                agents_bal_btn.visibility = View.VISIBLE
                available_balance_label.visibility = View.VISIBLE

                available_balance.text = availableOut
                balance.text = balanceOut
            } else {
                response.responseMessage ?: return@launch showNetworkError()

                showError(response.responseMessage)
            }
        }
    }
}



