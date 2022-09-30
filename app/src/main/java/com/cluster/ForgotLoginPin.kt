package com.cluster

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import com.cluster.core.data.api.AuthService
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.*
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debugOnly
import com.cluster.core.util.safeRunIO
import com.cluster.databinding.ActivityAgentActivationBinding
import com.cluster.databinding.ForgotLoginPinBinding
import com.cluster.pos.InvalidRemoteConnectionInfo
import com.cluster.pos.Platform
import com.cluster.pos.TerminalOptionsActivity
import com.cluster.pos.extension.posConfig
import com.cluster.pos.extension.posParameter
import com.cluster.pos.model.PosTenant
import com.cluster.ui.dataBinding
import com.cluster.utility.logout
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

private const val PASSWORD_LENGTH = 6

class ForgotLoginPinActivity : CreditClubActivity(R.layout.forgot_login_pin) {
    private val binding: ForgotLoginPinBinding by dataBinding()

    private var isResetPassword = false
    private var isConfirmOTP = false
    private var institutionCode: String = ""
    private val jsonPrefs by lazy { getSharedPreferences("JSON_STORAGE", 0) }
    private val staticService: StaticService by retrofitService()
    private val authService: AuthService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debugOnly {
            binding.skipButton.visibility = View.VISIBLE
            binding.skipButton.setOnClickListener {
                val phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')
                if (phoneNumber.isEmpty()) {
                    dialogProvider.indicateError(
                        "You did not enter your phone number",
                        binding.phoneNumberEt
                    )
                    return@setOnClickListener
                }


                localStorage.institutionCode = institutionCode
                localStorage.agentPhone = phoneNumber
                localStorage.cacheAuth = Json.encodeToString(
                    AuthResponse.serializer(),
                    AuthResponse(phoneNumber, institutionCode)
                )
                localStorage.putString("ACTIVATED", "ACTIVATED")
                localStorage.putString("AGENT_CODE", institutionCode)

                logout(jsonPrefs)
            }
        }

        if (Platform.isPOS) {
            binding.btnTerminalOptions.visibility = View.VISIBLE
            binding.btnTerminalOptions.setOnClickListener {
                adminAction {
                    startActivity(Intent(this, TerminalOptionsActivity::class.java))
                }
            }
        }

        binding.submitBtn.setOnClickListener {
            mainScope.launch {
                if (isResetPassword) {
                    activate()
                } else if(isConfirmOTP){
                    confirmOTP()
                } else {
                    validateOTP()
                }
            }
        }

        val phoneNumber = intent.getStringExtra("phone_number")
        if (phoneNumber != null) {
            binding.phoneNumberEt.apply {
                setText(phoneNumber)
                isEnabled = false
            }
        }
    }

    private suspend fun activate() {
        val phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')
        val requestedOTP = binding.verifyOtp.text.toString().trim()
        val password = binding.newPasswordEt.text.toString().trim(' ')
        val passwordConfirmation = binding.newPasswordConfirmationEt.text.toString().trim()

        if (password.length != PASSWORD_LENGTH) {
            dialogProvider.indicateError(
                "Your login PIN must have $PASSWORD_LENGTH digits",
                binding.newPasswordEt
            )
            firebaseCrashlytics.recordException(Exception("login PIN was not $PASSWORD_LENGTH digits"))
            return
        }

        if (password != passwordConfirmation) {
            dialogProvider.indicateError(
                "Login PINs do not match",
                binding.newPasswordConfirmationEt
            )
            return
        }


        val resetLoginPin = ResetLoginPin(
            loginPin = password,
            requestOTP = requestedOTP,
            phoneNumber = phoneNumber,
            institutionCode = localStorage.institutionCode
        )

        dialogProvider.showProgressBar("Sending Reset Pin Request")
        val (response, error) = safeRunIO {
            authService.resetLoginPinOTP(resetLoginPin)
        }
        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showError(error)
            return
        }
        if (response == null) {
            return showNetworkError()
        }

        if (response.isSuccessful) {
            localStorage.institutionCode = institutionCode
            localStorage.agentPhone = phoneNumber
            localStorage.putString("PIN RESET", "PIN RESET")

            firebaseAnalytics.logEvent("activation", Bundle().apply {
                putString("agent_code", localStorage.agent?.agentCode)
                putString("institution_code", institutionCode)
                putString("phone_number", phoneNumber)
            })

            dialogProvider.showSuccess(response.responseMessage)
            syncAgentInfo()
            firebaseAnalytics.setUserId(localStorage.agent?.agentCode)
            logout(jsonPrefs)
        } else {
            response.responseMessage ?: return showNetworkError()
            dialogProvider.showError(response.responseMessage)
        }
    }

    private suspend fun syncAgentInfo(): Boolean {
        val (agent, error) = safeRunIO {
            staticService.getAgentInfoByPhoneNumber(
                localStorage.institutionCode,
                localStorage.agentPhone,
            )
        }

        if (error != null || agent == null) return false

        localStorage.agent = agent
        firebaseCrashlytics.setUserId(agent.agentCode ?: "0")
        firebaseCrashlytics.setCustomKey("agent_name", agent.agentName ?: "")
        firebaseCrashlytics.setCustomKey("agent_phone", agent.phoneNumber ?: "")
        firebaseCrashlytics.setCustomKey("terminal_id", agent.terminalID ?: "")

        if (Platform.isPOS) {
            val posTenant: PosTenant by inject()
            posConfig.remoteConnectionInfo =
                posTenant.infoList.find { it.id == agent.posMode } ?: InvalidRemoteConnectionInfo
            posConfig.terminalId = agent.terminalID ?: ""
            posParameter.reset()
        }

        return true
    }

    private suspend fun validateOTP() {
        val phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')
        if (phoneNumber.isEmpty()) {
            dialogProvider.indicateError(
                "You did not enter your phone number",
                binding.phoneNumberEt
            )
            firebaseCrashlytics.recordException(Exception("No phone number was entered"))
            return
        }

        val sendLoginPinRequestOTP = SendLoginPinRequestOTP(
            phoneNumber = phoneNumber,
            institutionCode = localStorage.institutionCode!!
        )

        dialogProvider.showProgressBar("Sending OTP..")
        val (response, error) = safeRunIO {
            authService.sendLoginPinResetOTP(sendLoginPinRequestOTP)
        }
        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showError(error)
            return
        }
        if (response == null) {
            return showNetworkError()
        }

        if (response.isSuccessful) {
            isConfirmOTP = true

            binding.verifyOtp.visibility = View.VISIBLE
            binding.verifyOtp.requestFocus()
            binding.submitBtn.text = "Confirm OTP"
            binding.phoneNumberEt.visibility = View.GONE
            binding.instructionTv.text = getString(R.string.confirmOTP)

            dialogProvider.showSuccess(response.responseMessage)
        } else {
            response.responseMessage ?: return showNetworkError()
            dialogProvider.showError(response.responseMessage)
        }
    }

    private suspend fun confirmOTP(){
        val requestedOTP = binding.verifyOtp.text.toString().trim(' ')
        val phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')

        if (requestedOTP.isEmpty()) {
            dialogProvider.indicateError(
                "You did not enter your phone number",
                binding.verifyOtp
            )
            firebaseCrashlytics.recordException(Exception("No OTP was entered"))
            return
        }

        val confirmLoginPinOTP = ConfirmLoginPinOTP(
            requestOTP = requestedOTP,
            phoneNumber = phoneNumber,
            institutionCode = localStorage.institutionCode
        )

        dialogProvider.showProgressBar("Validating OTP..")
        val(response, error) = safeRunIO {
            authService.confirmLoginPinOTP(confirmLoginPinOTP)
        }

        if (error != null) {
            dialogProvider.showError(error)
            return
        }

        if (response == null) {
            return showNetworkError()
        }

        if(response.isSuccessful){
            isResetPassword = true

            binding.verifyOtp.visibility = View.GONE
            binding.pinLayout.visibility = View.VISIBLE
            binding.submitBtn.text = "Reset Password"
            binding.instructionTv.text = "Reset Login Pin"

            dialogProvider.showSuccess(response.responseMessage)
        } else {
            response.responseMessage ?: return showNetworkError()
            dialogProvider.showError(response.responseMessage)
        }

    }
    override fun onBackPressed() {
        dialogProvider.confirm("Cancel Pin Reset", "Are you sure you want to close this page?") {
            onSubmit {
                if (it) finish()
            }
        }
    }

    private inline fun adminAction(crossinline next: () -> Unit) {
        val passwordType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        com.cluster.pos.widget.Dialogs.input(
            this,
            "Administrator password",
            passwordType
        ) {
            onSubmit { password ->
                dismiss()
                if (password == posConfig.adminPin) next()
                else dialogProvider.showError("Incorrect Password")
            }
        }
    }
}

