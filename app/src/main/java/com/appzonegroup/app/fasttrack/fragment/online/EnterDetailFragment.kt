package com.appzonegroup.app.fasttrack.fragment.online

import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.appzonegroup.app.fasttrack.BankOneApplication
import com.appzonegroup.app.fasttrack.OnlineActivity
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentEnterDetailBinding
import com.appzonegroup.app.fasttrack.model.TransactionCountType
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.model.online.Response.fixResponse
import com.appzonegroup.app.fasttrack.network.online.APIHelper
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages
import com.appzonegroup.app.fasttrack.utility.online.convertXmlToJson
import com.creditclub.core.data.Encryption.decrypt
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.safeRun
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeoutException

class EnterDetailFragment : CreditClubFragment(R.layout.fragment_enter_detail) {
    private val authResponse: AuthResponse by lazy {
        (requireActivity().application as BankOneApplication).authResponse
    }
    private val binding by dataBinding<FragmentEnterDetailBinding>()
    private val ah by lazy { APIHelper(requireActivity()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        OptionsText.applyOptions(optionsText!!, binding.eText)
        OnlineActivity.isHome = false
        binding.upperHint.text = optionsText!!.hintText
        binding.btnActivate.setOnClickListener {
            mainScope.launch { activate() }
        }
    }

    private suspend fun activate() {
        val txt = binding.eText.text.toString().trim { it <= ' ' }
        if (txt.isBlank()) {
            Toast.makeText(activity, "Input cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        dialogProvider.showProgressBar("Loading")
        val (result, e) = ah.getNextOperation(
            authResponse.phoneNumber,
            authResponse.sessionId,
            txt,
            localStorage.lastKnownLocation ?: "0.00;0.00",
        )
        dialogProvider.hideProgressBar()

        if (e == null) {
            processData(result, txt)
            return
        }

        if (e is TimeoutException) {
            dialogProvider.confirm(
                "Something went wrong!",
                "Please try again",
            ) {
                onSubmit {
                    mainScope.launch { nextOperation(txt) }
                }
            }
        } else {
            Misc.increaseTransactionMonitorCounter(
                activity,
                TransactionCountType.NO_INTERNET_COUNT,
                authResponse.sessionId
            )
            dialogProvider.showError("Connection lost")
        }
    }

    private suspend fun nextOperation(txt: String) {
        dialogProvider.showProgressBar("Loading")
        val (result, error) = ah.getNextOperation(
            authResponse.phoneNumber,
            authResponse.sessionId,
            txt,
            localStorage.lastKnownLocation ?: "0.00;0.00",
        )
        dialogProvider.hideProgressBar()
        if (error == null) {
            processData(result, txt)
            return
        }
        Misc.increaseTransactionMonitorCounter(
            activity,
            TransactionCountType.NO_INTERNET_COUNT,
            authResponse.sessionId
        )
        dialogProvider.showError("Connection lost")
    }

    private fun processData(result: String?, txt: String) {
        try {
            val answer = fixResponse(result!!)
            val decryptedAnswer = decrypt(answer)
            val response = convertXmlToJson(decryptedAnswer)

            if (response == null) {
                Misc.increaseTransactionMonitorCounter(
                    activity, TransactionCountType.NO_INTERNET_COUNT, authResponse.sessionId
                )
                dialogProvider.showError("Connection lost")
                return
            }

            val resp = response.toString()
            val responseBase = response.getJSONObject("Response")
            val shouldClose = responseBase.optInt("ShouldClose", 1)
            if (shouldClose == 0) {
                if (resp.contains("IN-CORRECT ACTIVATION CODE") && state) {
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.ERROR_RESPONSE_COUNT,
                        authResponse.sessionId
                    )
                    localStorage.cacheAuth = null
                } else if (state) {
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.ERROR_RESPONSE_COUNT,
                        authResponse.sessionId
                    )
                    val (phoneNumber, sessionId) = (requireActivity().application as BankOneApplication).authResponse
                    val auth = JSONObject().apply {
                        put("phone_number", phoneNumber)
                        put("session_id", sessionId)
                        put("activation_code", txt)
                    }
                    localStorage.cacheAuth = auth.toString()
                }
                if (resp.contains("MenuItem")) {
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.SUCCESS_COUNT,
                        authResponse.sessionId,
                    )
                    val menuWrapper = responseBase
                        .getJSONObject("Menu")
                        .getJSONObject("Response")
                        .getJSONObject("Display")
                    requireActivity()
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(
                            R.id.container,
                            ListOptionsFragment.instantiate(menuWrapper, false),
                        ).commit()
                    return
                }

                val menuWrapper = responseBase
                    .getJSONObject("Menu")
                    .getJSONObject("Response")["Display"]
                if (menuWrapper is String && resp.contains("ShouldMask") && !resp.contains(
                        "Invalid Response"
                    )
                ) {
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.SUCCESS_COUNT,
                        authResponse.sessionId
                    )
                    val data = responseBase
                        .getJSONObject("Menu")
                        .getJSONObject("Response")
                    if (resp.contains("\"IsImage\":\"true\"")) {
                        requireActivity()
                            .supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.container,
                                CustomerImageFragment.instantiate(data)
                            ).commit()
                    } else {
                        requireActivity()
                            .supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, instantiate(data))
                            .commit()
                    }
                } else {
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.ERROR_RESPONSE_COUNT,
                        authResponse.sessionId
                    )
                    val message = responseBase
                        .getJSONObject("Menu")
                        .getJSONObject("Response")
                        .getString("Display")
                    dialogProvider.showError(Html.fromHtml(message))
                }
            } else {
                Misc.increaseTransactionMonitorCounter(
                    activity,
                    TransactionCountType.ERROR_RESPONSE_COUNT,
                    authResponse.sessionId
                )
                if (responseBase.toString().contains("Display")) {
                    dialogProvider.showError(
                        responseBase
                            .getJSONObject("Menu")
                            .getJSONObject("Response")
                            .optString("Display", ErrorMessages.PHONE_NOT_REGISTERED)
                    ) {
                        onClose {
                            (activity as OnlineActivity?)!!.goHome()
                        }
                    }
                } else {
                    dialogProvider.showError(
                        Html.fromHtml(
                            responseBase.optString(
                                "Menu",
                                ErrorMessages.PHONE_NOT_REGISTERED
                            )
                        )
                    )
                }
            }

        } catch (c: Exception) {
            Misc.increaseTransactionMonitorCounter(
                activity,
                TransactionCountType.NO_INTERNET_COUNT,
                authResponse.sessionId
            )
            dialogProvider.confirm("Something went wrong!", "Please try again") {
                onSubmit {
                    mainScope.launch { continueNextOperation() }
                }

                onClose {
                    (activity as OnlineActivity?)!!.goHome()
                }
            }
        }
    }

    private suspend fun continueNextOperation() {
        dialogProvider.showProgressBar("Loading")
        val txt = binding.eText.text.toString().trim { it <= ' ' }
        val (result, error) = ah.continueNextOperation(
            authResponse.phoneNumber,
            authResponse.sessionId,
            txt,
            localStorage.lastKnownLocation ?: "0.00;0.00",
        )
        dialogProvider.hideProgressBar()
        if (error != null) {
            Misc.increaseTransactionMonitorCounter(
                activity,
                TransactionCountType.NO_INTERNET_COUNT,
                (requireActivity().application as BankOneApplication).authResponse.sessionId
            )
            dialogProvider.showError("Connection lost")
            return
        }

        processData(result, txt)
    }

    class OptionsText(jo: JSONObject) {
        var isNumeric = false
        var isImage = false
        var isShouldMask = false
        var isShouldCompress = false
        var hintText: String? = null

        companion object {
            fun applyOptions(otxt: OptionsText, eText: EditText) {
                //eText.setHint(otxt.getHintText());
                if (!otxt.isNumeric && otxt.isShouldMask) {
                    eText.transformationMethod = PasswordTransformationMethod.getInstance()
                    eText.inputType = InputType.TYPE_CLASS_TEXT
                } else {
                    if (otxt.isNumeric && otxt.isShouldMask) {
                        eText.transformationMethod = PasswordTransformationMethod.getInstance()
                        eText.inputType =
                            if (otxt.isNumeric) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT
                    } else if (!otxt.isShouldMask && otxt.isNumeric) {
                        eText.inputType =
                            if (otxt.isNumeric) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT
                    } else if (!otxt.isNumeric) {
                        eText.inputType = InputType.TYPE_CLASS_TEXT
                    }
                }
            }
        }

        init {
            safeRun {
                isNumeric = jo.getJSONObject("IsNumeric").getBoolean("IsNumeric")
                isShouldMask = jo.getJSONObject("ShouldMask").getBoolean("Mask")
                hintText = jo.optString("Display").uppercase(Locale.ROOT)
            }
        }
    }

    companion object {
        var state = false
        var optionsText: OptionsText? = null
        fun instantiate(data: JSONObject): EnterDetailFragment {
            optionsText = OptionsText(data)
            return EnterDetailFragment()
        }

        @JvmStatic
        fun instantiate(data: JSONObject, value: Boolean): EnterDetailFragment {
            optionsText = OptionsText(data)
            state = value
            return EnterDetailFragment()
        }
    }
}