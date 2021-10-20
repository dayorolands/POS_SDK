package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import com.appzonegroup.app.fasttrack.databinding.ActivityAgentActivationBinding
import com.appzonegroup.app.fasttrack.utility.logout
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.TerminalOptionsActivity
import com.appzonegroup.creditclub.pos.extension.posConfig
import com.appzonegroup.creditclub.pos.extension.posParameter
import com.appzonegroup.creditclub.pos.extension.posSerialNumber
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.AuthResponse
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.request.PinChangeRequest
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.InvalidRemoteConnectionInfo
import com.creditclub.pos.model.PosTenant
import com.creditclub.ui.dataBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

class AgentActivationActivity : CreditClubActivity(R.layout.activity_agent_activation) {
    private val binding by dataBinding<ActivityAgentActivationBinding>()

    private var isActivation = false
    private var code = ""
    private var institutionCode: String = ""
    private var phoneNumber = ""
    private var pin = ""
    private val appDataStorage: AppDataStorage by inject()
    private val staticService: StaticService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        debugOnly {
            binding.skipButton.visibility = View.VISIBLE
            binding.skipButton.setOnClickListener(View.OnClickListener {
                phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')

                if (phoneNumber.isEmpty()) {
                    dialogProvider.indicateError(
                        "You did not enter your phone number",
                        binding.phoneNumberEt
                    )
                    return@OnClickListener
                }

                code = binding.codeEt.value
                if (code.length < 6) {
                    dialogProvider.indicateError(
                        "Enter institution code in the verification code input",
                        binding.codeEt
                    )
                    return@OnClickListener
                }

                localStorage.institutionCode = code
                localStorage.agentPhone = phoneNumber
                localStorage.cacheAuth = Json.encodeToString(
                    AuthResponse.serializer(),
                    AuthResponse(phoneNumber, code)
                )
                localStorage.putString("ACTIVATED", "ACTIVATED")
                localStorage.putString("AGENT_CODE", code)

                logout()
            })
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
            mainScope.launch { submit() }
        }

        val phoneNumber = intent.getStringExtra("phone_number")
        if (phoneNumber != null) {
            binding.phoneNumberEt.apply {
                setText(phoneNumber)
                isEnabled = false
            }
        }
    }

    private suspend fun submit() {
        code = binding.codeEt.text.toString().trim(' ')
        phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')

        if (phoneNumber.isEmpty()) {
            dialogProvider.indicateError(
                "You did not enter your phone number",
                binding.phoneNumberEt
            )
            firebaseCrashlytics.recordException(Exception("No phone number was entered"))
            return
        }

        if (code.isEmpty() && !isActivation) {
            dialogProvider.indicateError(
                "You did not enter your verification code",
                binding.codeEt
            )
            firebaseCrashlytics.recordException(Exception("verification code not inputted"))
            return
        }

        if (isActivation && binding.newPinEt.text.toString().isEmpty()) {
            dialogProvider.indicateError("You did not enter the PIN", binding.newPinEt)
            firebaseCrashlytics.recordException(Exception("No PIN was entered"))
            return
        }

        if (isActivation && binding.newPinConfirmationEt.text.toString().isEmpty()) {
            dialogProvider.indicateError(
                "You did not confirm the PIN",
                binding.newPinConfirmationEt
            )
            firebaseCrashlytics.recordException(Exception("PIN entry was not confirmed"))
            return
        }

        if (isActivation && binding.newPinEt.text.toString().length != 4) {
            dialogProvider.indicateError("Your PIN must have 4-digit", binding.newPinEt)
            firebaseCrashlytics.recordException(Exception("Pin was not 4 digits"))
            return
        }

        if (isActivation && binding.newPinConfirmationEt.text.toString().length != 4) {
            dialogProvider.indicateError(
                "Your PIN Confirmation must have 4-digit",
                binding.newPinConfirmationEt
            )
            firebaseCrashlytics.recordException(Exception("confirmation pin was not 4 digits"))
            return
        }

        if (binding.newPinConfirmationEt.text.toString() != binding.newPinEt.text.toString()) {
            dialogProvider.indicateError("PIN mismatch", binding.newPinConfirmationEt)
            return
        }

        pin = binding.newPinEt.text.toString()

        dialogProvider.showProgressBar("Processing...")

        if (isActivation) activate()
        else verify()
    }

    private suspend inline fun activate() {
        val request = PinChangeRequest(
            activationCode = code,
            institutionCode = institutionCode,
            agentPhoneNumber = phoneNumber,
            confirmNewPin = pin,
            newPin = pin,
            geoLocation = localStorage.lastKnownLocation,
            oldPin = code,
            deviceId = appDataStorage.deviceId,
        )

        dialogProvider.showProgressBar("Activating")
        val (response, error) = safeRunIO {
            staticService.completeActivationWithPinChange(request)
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
                AuthResponse(phoneNumber, code)
            )
            localStorage.putString("ACTIVATED", "ACTIVATED")
            localStorage.putString("AGENT_CODE", code)

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

    private suspend inline fun verify() {
        dialogProvider.showProgressBar("Verifying")
        val (response, error) = safeRunIO {
            staticService.agentVerification(
                code,
                phoneNumber,
                institutionCode,
                if (Platform.isPOS) posSerialNumber else appDataStorage.deviceId
            )
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
                response.responseMessage ?: if (code.length >= 6) code.substring(0, 6) else code

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

        com.appzonegroup.creditclub.pos.widget.Dialogs.input(
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

