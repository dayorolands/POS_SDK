package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.appzonegroup.app.fasttrack.databinding.ActivityAgentActivationBinding
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.LoadDataType
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.TerminalOptionsActivity
import com.crashlytics.android.Crashlytics
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
    override val hasLogoutTimer = false
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

                localStorage.institutionCode = code.substring(0, 6)
                localStorage.agentPhone = phoneNumber
                localStorage.cacheAuth = Gson().toJson(AuthResponse(phoneNumber, "1111"))
                localStorage.putString(AppConstants.ACTIVATED, AppConstants.ACTIVATED)
                localStorage.putString(AppConstants.AGENT_CODE, "1111")

                val intent = Intent(this@AgentActivationActivity, DataLoaderActivity::class.java)
                intent.putExtra(AppConstants.LOAD_DATA, LoadDataType.OTHER_DATA.ordinal)
                startActivity(intent)
                finish()
            })
        }

        if (Platform.isPOS) {
//            Platform.setup(this)

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
            Crashlytics.logException(Exception("No phone number was entered"))
            return
        }

        if (code.isEmpty() && !isActivation) {
            indicateError(
                "You did not enter your verification code",
                binding.codeEt
            )
            Crashlytics.logException(Exception("verification code not inputted"))
            return
        }

        /*if (isActivation && ((EditText)findViewById(R.id.agentActivation_oldPINEt)).getText().toString().length() == 0){
            indicateError("You did not enter your old PIN", ((EditText)findViewById(R.id.agentActivation_PINEt)));
            Crashlytics.logException(new Exception("No PIN was entered"));
            return;
        }*/

        if (isActivation && binding.newPinEt.text.toString().isEmpty()) {
            indicateError("You did not enter the PIN", binding.newPinEt)
            Crashlytics.logException(Exception("No PIN was entered"))
            return
        }

        if (isActivation && binding.newPinConfirmationEt.text.toString().isEmpty()) {
            indicateError("You did not confirm the PIN", binding.newPinConfirmationEt)
            Crashlytics.logException(Exception("PIN entry was not confirmed"))
            return
        }

        if (isActivation && binding.newPinEt.text.toString().length != 4) {
            indicateError("Your PIN must have 4-digit", binding.newPinEt)
            Crashlytics.logException(Exception("Pin was not 4 digits"))
            return
        }

        if (isActivation && binding.newPinConfirmationEt.text.toString().length != 4) {
            indicateError(
                "Your PIN Confirmation must have 4-digit",
                binding.newPinConfirmationEt
            )
            Crashlytics.logException(Exception("confirmation pin was not 4 digits"))
            return
        }

        if (binding.newPinConfirmationEt.text.toString() != binding.newPinEt.text.toString()) {
            indicateError("PIN mismatch", binding.newPinConfirmationEt)
            return
        }

        pin = binding.newPinEt.text.toString()

        showProgressBar("Processing...")

        if (isActivation) {

            val request = PinChangeRequest()
            request.activationCode = code
            request.institutionCode = institutionCode
            request.agentPhoneNumber = phoneNumber
            request.confirmNewPin = pin
            request.newPin = pin
            request.geoLocation = ""
            request.oldPin = code
            request.deviceId = deviceId

            mainScope.launch {
                showProgressBar("Activating")
                val (response) = safeRunIO {
                    creditClubMiddleWareAPI.staticService.completeActivationWithPinChange(request)
                }
                hideProgressBar()

                response ?: return@launch showNetworkError()

                if (response.isSuccessful) {
                    localStorage.institutionCode = institutionCode
                    localStorage.agentPhone = phoneNumber
                    localStorage.agentPIN = pin
                    localStorage.cacheAuth = Gson().toJson(AuthResponse(phoneNumber, code))
                    localStorage.putString(AppConstants.ACTIVATED, AppConstants.ACTIVATED)
                    localStorage.putString(AppConstants.AGENT_CODE, code)

                    val intent =
                        Intent(this@AgentActivationActivity, DataLoaderActivity::class.java)
                    intent.putExtra(AppConstants.LOAD_DATA, LoadDataType.OTHER_DATA.ordinal)
                    startActivity(intent)
                    finish()
                    showNotification("Activation was successful")
                } else {
                    response.responseMessage ?: return@launch showNetworkError()

                    showError(response.responseMessage)
                }
            }
        } else {
            mainScope.launch {
                showProgressBar("Verifying")
                val (response) = safeRunIO {
                    creditClubMiddleWareAPI.staticService.agentVerification(
                        code,
                        phoneNumber,
                        institutionCode,
                        deviceId
                    )
                }
                hideProgressBar()

                response ?: return@launch showNetworkError()

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
                        response.responseMessage ?: if (code.length >= 6) code.substring(
                            0,
                            6
                        ) else code

                    showNotification("Verification successful")
                } else {
                    response.responseMessage ?: return@launch showNetworkError()

                    showError(response.responseMessage)
                }
            }
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Closing Activity")
            .setMessage("Are you sure you want to close this activity?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }
}

