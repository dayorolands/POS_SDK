package com.cluster

import android.os.Bundle
import com.cluster.databinding.ActivityChangePinBinding
import com.cluster.utility.FunctionIds
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.request.PinChangeRequest
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.safeRunIO
import com.cluster.ui.dataBinding
import kotlinx.coroutines.launch

class ChangePinActivity : CreditClubActivity(R.layout.activity_change_pin) {
    private val staticService: StaticService by retrofitService()
    override val functionId = FunctionIds.AGENT_CHANGE_PIN
    private val binding: ActivityChangePinBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.changePinBtn.setOnClickListener {
            mainScope.launch { changePin() }
        }
    }

    private suspend fun changePin() {
        val oldPin = binding.oldPin.value
        if (oldPin.isEmpty()) {
            dialogProvider.showError("Please enter the customer's old PIN")
            return
        }
        if (oldPin.length != 4) {
            dialogProvider.showError("Please enter the complete PIN")
            return
        }
        val newPin = binding.newPin.value
        if (newPin.isEmpty()) {
            dialogProvider.showError("Please enter your PIN")
            return
        }
        if (newPin.length != 4) {
            dialogProvider.showError("Please enter the complete new PIN")
            return
        }
        val confirmNewPin = binding.confirmNewPin.value
        if (confirmNewPin != newPin) {
            dialogProvider.showError(getString(R.string.new_pin_confirmation_mismatch))
            return
        }
        val changePinRequest = PinChangeRequest(
            agentPhoneNumber = localStorage.agentPhone,
            activationCode = localStorage.agent!!.agentCode,
            institutionCode = localStorage.institutionCode,
            newPin = newPin,
            confirmNewPin = confirmNewPin,
            oldPin = oldPin,
            geoLocation = localStorage.lastKnownLocation,
        )

        val (response, error) = safeRunIO {
            staticService.pinChange(changePinRequest)
        }
        if (error != null) {
            dialogProvider.showError(error)
            return
        }
        if (response!!.isSuccessful) {
            dialogProvider.showSuccessAndWait("Your PIN was changed successfully")
            finish()
        } else {
            dialogProvider.showError(response.responseMessage)
        }
    }
}