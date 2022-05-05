package com.cluster

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import com.cluster.core.data.api.AuthService
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.ActivationRequest
import com.cluster.core.data.model.AuthResponse
import com.cluster.core.data.model.VerificationRequest
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debugOnly
import com.cluster.core.util.safeRunIO
import com.cluster.databinding.ActivityAgentActivationBinding
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
private const val PIN_LENGTH = 4

class AgentActivationActivity : CreditClubActivity(R.layout.activity_agent_activation) {
    private val binding: ActivityAgentActivationBinding by dataBinding()

    private var isActivation = false
    private var institutionCode: String = ""
    private val appDataStorage: AppDataStorage by inject()
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

                val institutionCode = binding.codeEt.value
                if (institutionCode.length != 6) {
                    dialogProvider.indicateError(
                        "Enter a valid institution code",
                        binding.codeEt
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

                logout()
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
                if (isActivation) {
                    activate()
                } else {
                    verify()
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
        val activationCode = binding.codeEt.text.toString().trim(' ')
        val phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')
        val pin = binding.newPinEt.text.toString().trim()
        val pinConfirmation = binding.newPinConfirmationEt.text.toString().trim()
        val password = binding.newPasswordEt.text.toString().trim()
        val passwordConfirmation = binding.newPasswordConfirmationEt.text.toString().trim()

        if (activationCode.isEmpty()) {
            dialogProvider.indicateError(
                "Enter an activation code",
                binding.codeEt
            )
            firebaseCrashlytics.recordException(Exception("activation code not inputted"))
            return
        }

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
        if (pin.length != PIN_LENGTH) {
            dialogProvider.indicateError("Your PIN must have $PIN_LENGTH digits", binding.newPinEt)
            firebaseCrashlytics.recordException(Exception("Pin was not $PIN_LENGTH digits"))
            return
        }
        if (pin != pinConfirmation) {
            dialogProvider.indicateError("PINs do not match", binding.newPinConfirmationEt)
            return
        }

        val request = ActivationRequest(
            activationCode = activationCode,
            institutionCode = institutionCode,
            agentPhoneNumber = phoneNumber,
            confirmNewTransactionPin = pin,
            newTransactionPin = pinConfirmation,
            geoLocation = localStorage.lastKnownLocation,
            deviceId = appDataStorage.deviceId!!,
            newLoginPassword = password,
            confirmLoginPassword = passwordConfirmation,
        )

        dialogProvider.showProgressBar("Activating")
        val (response, error) = safeRunIO {
            authService.activate(request)
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
            localStorage.transactionSequenceNumber = response.transactionSequenceNumber
            localStorage.deviceNumber = response.deviceNumber
            localStorage.institutionCode = institutionCode
            localStorage.agentPhone = phoneNumber
            localStorage.cacheAuth = Json.encodeToString(
                AuthResponse.serializer(),
                AuthResponse(phoneNumber, activationCode)
            )
            localStorage.putString("ACTIVATED", "ACTIVATED")
            localStorage.putString("AGENT_CODE", activationCode)

            firebaseAnalytics.logEvent("activation", Bundle().apply {
                putString("agent_code", localStorage.agent?.agentCode)
                putString("institution_code", institutionCode)
                putString("phone_number", phoneNumber)
            })

            syncAgentInfo()
            firebaseAnalytics.setUserId(localStorage.agent?.agentCode)

            logout()
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

    private suspend fun verify() {
        val verificationCode = binding.codeEt.text.toString().trim(' ')
        val phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')
        if (phoneNumber.isEmpty()) {
            dialogProvider.indicateError(
                "You did not enter your phone number",
                binding.phoneNumberEt
            )
            firebaseCrashlytics.recordException(Exception("No phone number was entered"))
            return
        }

        if (verificationCode.isEmpty()) {
            dialogProvider.indicateError(
                "You did not enter your verification code",
                binding.codeEt
            )
            firebaseCrashlytics.recordException(Exception("verification code not inputted"))
            return
        }

        val verificationRequest = VerificationRequest(
            verificationCode = verificationCode,
            agentPhoneNumber = phoneNumber,
            deviceId = appDataStorage.deviceId!!,
        )

        dialogProvider.showProgressBar("Verifying")
        val (response, error) = safeRunIO {
            authService.verify(verificationRequest)
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
            isActivation = true

            binding.pinLayout.visibility = View.VISIBLE
            binding.phoneNumberEt.visibility = View.GONE
            binding.instructionTv.text = getString(R.string.activateAccount)
            binding.codeEt.hint = getString(R.string.enter_activation_code)
            binding.codeEt.setText("")
            binding.codeEt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(4))
            binding.codeEt.requestFocus()
            institutionCode =
                response.responseMessage
                    ?: if (verificationCode.length >= 6) {
                        verificationCode.substring(0, 6)
                    } else {
                        verificationCode
                    }

            dialogProvider.showSuccess("Verification successful")
        } else {
            response.responseMessage ?: return showNetworkError()

            dialogProvider.showError(response.responseMessage)
        }
    }

    override fun onBackPressed() {
        dialogProvider.confirm("Cancel Activation", "Are you sure you want to close this page?") {
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

