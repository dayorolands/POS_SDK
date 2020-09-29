package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.view.View
import com.android.volley.Response
import com.appzonegroup.app.fasttrack.databinding.ActivityAgentActivationBinding
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.utility.logout
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.TerminalOptionsActivity
import com.appzonegroup.creditclub.pos.extension.posConfig
import com.appzonegroup.creditclub.pos.extension.posParameter
import com.appzonegroup.creditclub.pos.extension.posSerialNumber
import com.creditclub.core.data.request.PinChangeRequest
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AgentActivationActivity : BaseActivity() {
    val binding by contentView<AgentActivationActivity, ActivityAgentActivationBinding>(R.layout.activity_agent_activation)

    private var isActivation = false
    internal var url = ""
    internal var json = ""
    internal var code = ""
    internal var response: Response<*>? = null
    internal var institutionCode: String = ""
    var phoneNumber = ""
    var pin = ""
    override val hasLogoutTimer get() = false
    private val deviceId
        get() = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding

        debugOnly {
            binding.skipButton.visibility = View.VISIBLE
            binding.skipButton.setOnClickListener(View.OnClickListener {
                phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')

                if (phoneNumber.isEmpty()) {
                    indicateError("You did not enter your phone number", binding.phoneNumberEt)
                    return@OnClickListener
                }

                code = binding.codeEt.value
                if (code.length < 6) {
                    indicateError(
                        "Enter institution code in the verification code input",
                        binding.codeEt
                    )
                    return@OnClickListener
                }

                localStorage.institutionCode = code
                localStorage.agentPhone = phoneNumber
                localStorage.cacheAuth = Gson().toJson(AuthResponse(phoneNumber, code))
                localStorage.putString(AppConstants.ACTIVATED, AppConstants.ACTIVATED)
                localStorage.putString(AppConstants.AGENT_CODE, code)
                localStorage.putString(AppConstants.AGENT_PIN, "1111")

                logout()
            })
        }

        if (Platform.isPOS) {
            binding.btnTerminalOptions.visibility = View.VISIBLE
            binding.btnTerminalOptions.setOnClickListener {
                adminAction {
                    startActivity(TerminalOptionsActivity::class.java)
                }
            }
        }
    }

    fun submit_click(view: View) {

        code = binding.codeEt.text.toString().trim(' ')
        phoneNumber = binding.phoneNumberEt.text.toString().trim(' ')

        if (phoneNumber.isEmpty()) {
            indicateError(
                "You did not enter your phone number",
                binding.phoneNumberEt
            )
            firebaseCrashlytics.recordException(Exception("No phone number was entered"))
            return
        }

        if (code.isEmpty() && !isActivation) {
            indicateError(
                "You did not enter your verification code",
                binding.codeEt
            )
            firebaseCrashlytics.recordException(Exception("verification code not inputted"))
            return
        }

        /*if (isActivation && ((EditText)findViewById(R.id.agentActivation_oldPINEt)).getText().toString().length() == 0){
            indicateError("You did not enter your old PIN", ((EditText)findViewById(R.id.agentActivation_PINEt)));
            firebaseCrashlytics.recordException(new Exception("No PIN was entered"));
            return;
        }*/

        if (isActivation && binding.newPinEt.text.toString().isEmpty()) {
            indicateError("You did not enter the PIN", binding.newPinEt)
            firebaseCrashlytics.recordException(Exception("No PIN was entered"))
            return
        }

        if (isActivation && binding.newPinConfirmationEt.text.toString().isEmpty()) {
            indicateError("You did not confirm the PIN", binding.newPinConfirmationEt)
            firebaseCrashlytics.recordException(Exception("PIN entry was not confirmed"))
            return
        }

        if (isActivation && binding.newPinEt.text.toString().length != 4) {
            indicateError("Your PIN must have 4-digit", binding.newPinEt)
            firebaseCrashlytics.recordException(Exception("Pin was not 4 digits"))
            return
        }

        if (isActivation && binding.newPinConfirmationEt.text.toString().length != 4) {
            indicateError(
                "Your PIN Confirmation must have 4-digit",
                binding.newPinConfirmationEt
            )
            firebaseCrashlytics.recordException(Exception("confirmation pin was not 4 digits"))
            return
        }

        if (binding.newPinConfirmationEt.text.toString() != binding.newPinEt.text.toString()) {
            indicateError("PIN mismatch", binding.newPinConfirmationEt)
            return
        }

        pin = binding.newPinEt.text.toString()

        showProgressBar("Processing...")

        mainScope.launch {
            if (isActivation) activate()
            else verify()
        }
    }

    private suspend inline fun activate() {
        val request = PinChangeRequest()
        request.activationCode = code
        request.institutionCode = institutionCode
        request.agentPhoneNumber = phoneNumber
        request.confirmNewPin = pin
        request.newPin = pin
        request.geoLocation = ""
        request.oldPin = code
        request.deviceId = deviceId

        showProgressBar("Activating")
        val (response) = safeRunIO {
            creditClubMiddleWareAPI.staticService.completeActivationWithPinChange(request)
        }
        hideProgressBar()

        response ?: return showNetworkError()

        if (response.isSuccessful) {
            localStorage.institutionCode = institutionCode
            localStorage.agentPhone = phoneNumber
//            localStorage.agentPIN = pin
            localStorage.cacheAuth = Gson().toJson(AuthResponse(phoneNumber, code))
            localStorage.putString(AppConstants.ACTIVATED, AppConstants.ACTIVATED)
            localStorage.putString(AppConstants.AGENT_CODE, code)

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

            showError(response.responseMessage)
        }
    }

    private suspend fun syncAgentInfo(): Boolean {
        val (agent, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.getAgentInfoByPhoneNumber(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }

        if (error != null) return false
        agent ?: return false

        localStorage.agent = agent
        firebaseCrashlytics.setUserId(agent.agentCode ?: "0")
        firebaseCrashlytics.setCustomKey("agent_name", agent.agentName ?: "")
        firebaseCrashlytics.setCustomKey("agent_phone", agent.phoneNumber ?: "")
        firebaseCrashlytics.setCustomKey("terminal_id", agent.terminalID ?: "")

        if (Platform.isPOS) {
//            posConfig.posModeStr = agent.posMode
            posConfig.terminalId = agent.terminalID ?: ""
            posParameter.reset()
        }

        return true
    }

    private suspend inline fun verify() {
        showProgressBar("Verifying")
        val (response) = safeRunIO {
            creditClubMiddleWareAPI.staticService.agentVerification(
                code,
                phoneNumber,
                institutionCode,
                if (Platform.isPOS) posSerialNumber else deviceId
            )
        }
        hideProgressBar()

        response ?: return showNetworkError()

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

            showNotification("Verification successful")
        } else {
            response.responseMessage ?: return showNetworkError()

            showError(response.responseMessage)
        }
    }

    override fun onBackPressed() {
        dialogProvider.confirm("Cancel Activation", "Are you sure you want to close this page?") {
            onSubmit {
                if (it) finish()
            }
        }
    }
}

