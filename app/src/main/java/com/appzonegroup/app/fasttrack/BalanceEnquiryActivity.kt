package com.appzonegroup.app.fasttrack

import android.view.View
import com.appzonegroup.app.fasttrack.databinding.ActivityBalanceEnquiryBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.request.BalanceEnquiryRequest
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch

class BalanceEnquiryActivity : CreditClubActivity(R.layout.activity_balance_enquiry) {
    private val binding: ActivityBalanceEnquiryBinding by dataBinding()
    private var pin = ""
    override val functionId = FunctionIds.AGENT_BALANCE_ENQUIRY


    fun onClick(view: View) {
        pin = binding.pinEt.value
        binding.pinEt.value = ""

        if (pin.isEmpty()) {
            dialogProvider.showError("Please enter your PIN")
            return
        }

        if (pin.length != 4) {
            dialogProvider.showError("Agent PIN must be 4 digits")
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

            dialogProvider.showProgressBar("Getting Balance")
            val (response) = safeRunIO {
                creditClubMiddleWareAPI.staticService.balanceEnquiry(request)
            }
            dialogProvider.hideProgressBar()

            response ?: return@launch showNetworkError()

            if (response.isSussessful) {
                val balanceOut = "NGN${response.balance}"
                val availableOut = "${getString(R.string.naira)}${response.availableBalance}"

//                localStorage.agentPIN = pin

                binding.agentsBalBtn.visibility = View.VISIBLE
                binding.availableBalanceLabel.visibility = View.VISIBLE

                binding.availableBalance.text = availableOut
                binding.balance.text = balanceOut
            } else {
                response.responseMessage ?: return@launch showNetworkError()

                dialogProvider.showError(response.responseMessage)
            }
        }
    }
}



