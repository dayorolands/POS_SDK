package com.appzonegroup.app.fasttrack.fragment.online

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.appzonegroup.app.fasttrack.BankOneApplication
import com.appzonegroup.app.fasttrack.OnlineActivity
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentCustomerImageBinding
import com.appzonegroup.app.fasttrack.fragment.online.EnterDetailFragment.OptionsText
import com.appzonegroup.app.fasttrack.model.TransactionCountType
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.model.online.Response
import com.appzonegroup.app.fasttrack.network.online.APIHelper
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages
import com.appzonegroup.app.fasttrack.utility.online.convertXmlToJson
import com.creditclub.core.data.Encryption
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.safeRunIO
import com.esafirm.imagepicker.features.*
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.features.common.BaseConfig
import com.esafirm.imagepicker.helper.ConfigUtils
import com.esafirm.imagepicker.model.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeoutException

inline fun CreditClubFragment.registerImagePicker(
    crossinline callback: suspend CoroutineScope.(List<Image>) -> Unit
): (BaseConfig) -> Unit {
    val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val images = ImagePicker.getImages(it.data) ?: emptyList()
            mainScope.launch { callback(images) }
        }
    return { config: BaseConfig ->
        val finalConfig =
            if (config is ImagePickerConfig) ConfigUtils.checkConfig(config) else config
        val intent = createImagePickerIntent(requireContext(), finalConfig)
        activityResult.launch(intent)
    }
}

class CustomerImageFragment : CreditClubFragment(R.layout.fragment_customer_image),
    View.OnClickListener {
    private var creditClubImage: CreditClubImage? = null
    private var image: Bitmap? = null
    private val binding by dataBinding<FragmentCustomerImageBinding>()
    private var optionsText: OptionsText? = null
    private val authResponse: AuthResponse by lazy {
        (requireActivity().application as BankOneApplication).authResponse
    }
    private val ah by lazy { APIHelper(requireActivity()) }
    private val launcher = registerImagePicker {
        try {
            val tmpImage = it.firstOrNull()
                ?: return@registerImagePicker dialogProvider.showError("An internal error occurred")
            creditClubImage = CreditClubImage(requireContext(), tmpImage)

            dialogProvider.showProgressBar("Processing image")

            val (bitmap) = safeRunIO { creditClubImage?.bitmap }
            image = bitmap
            binding.image.setImageBitmap(bitmap)

            dialogProvider.hideProgressBar()
        } catch (ex: Exception) {
            ex.printStackTrace()
            dialogProvider.showError("An internal error occurred")
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        OnlineActivity.isHome = false
        binding.upperHint.text = optionsText?.hintText ?: ""
        binding.gallery.setOnClickListener(this)
        binding.takePhoto.setOnClickListener(this)
        binding.btnActivate.setOnClickListener(this)

        activity?.currentFocus?.let {
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onClick(view: View) {
        when (view) {
            binding.gallery -> launcher(
                ImagePickerConfig(
                    mode = ImagePickerMode.SINGLE,
                    isShowCamera = false,
                    isFolderMode = true,
                    returnMode = ReturnMode.ALL,
                )
            )

            binding.takePhoto -> launcher(CameraOnlyConfig())

            else -> {
                if (image == null) {
                    Toast.makeText(
                        activity,
                        "Please select or take a picture!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                mainScope.launch { nextOperation() }
            }
        }
    }

    private suspend fun nextOperation() {
        val finalFile = File(creditClubImage?.path ?: return)
        val bankOneApplication = activity?.application as BankOneApplication?
        val authResponse = bankOneApplication?.authResponse
        val ah = APIHelper(requireContext())

        dialogProvider.showProgressBar("Uploading")
        val (result, e) = ah.getNextOperationImage(
            authResponse?.phoneNumber ?: "",
            authResponse?.sessionId ?: "nothing",
            finalFile,
            localStorage.lastKnownLocation ?: "0.00;0.00",
            optionsText?.isShouldCompress ?: false,
        )
        dialogProvider.hideProgressBar()

        if (e == null && result != null) {
            try {
                val answer = Response.fixResponse(result)
                Log.e("Answer", answer)
                Log.e("AnswerLength", answer.length.toString() + "")
                if (TextUtils.isEmpty(answer.trim { it <= ' ' })) {
                    Toast.makeText(
                        activity,
                        "Upload successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.SUCCESS_COUNT,
                        authResponse?.sessionId
                    )
                    moveToNext()
                }
            } catch (c: Exception) {
                c.printStackTrace()
                showDialogWithGoHomeAction("Something went wrong! Please try again.")
                Misc.increaseTransactionMonitorCounter(
                    activity,
                    TransactionCountType.NO_RESPONSE_COUNT,
                    authResponse?.sessionId
                )
            }
        } else {
            if (e != null) {
                Log.e("ErrorResponse", e.toString())
                e.printStackTrace()
                if (e is TimeoutException) {
                    showDialogWithGoHomeAction("Something went wrong! Please try again.")
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.NO_RESPONSE_COUNT,
                        authResponse?.sessionId
                    )
                } else {
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.NO_INTERNET_COUNT,
                        authResponse?.sessionId
                    )
                    dialogProvider.showError("Connection lost")
                }
            } else {
                dialogProvider.showError("Connection lost")
                Misc.increaseTransactionMonitorCounter(
                    activity,
                    TransactionCountType.NO_INTERNET_COUNT,
                    authResponse?.sessionId
                )
            }
        }
    }

    private fun showDialogWithGoHomeAction(message: String?) {
        dialogProvider.showError(message) {
            onClose {
                (activity as OnlineActivity?)?.goHome()
            }
        }
    }

    private fun moveToNext() {
        dialogProvider.showProgressBar("Loading")
        ah.getNextOperation(
            authResponse.phoneNumber,
            authResponse.sessionId,
            creditClubImage?.path ?: "",
            localStorage.lastKnownLocation ?: "0.00;0.00",
        ) { e, result, status ->
            dialogProvider.hideProgressBar()
            if (status) {
                try {
                    val answer = Response.fixResponse(result!!)
                    val decryptedAnswer = Encryption.decrypt(answer)
                    val response = convertXmlToJson(decryptedAnswer)
                    if (response == null) {
                        dialogProvider.showError("Connection lost")
                        Misc.increaseTransactionMonitorCounter(
                            activity,
                            TransactionCountType.NO_INTERNET_COUNT,
                            authResponse.sessionId
                        )
                    } else {
                        val resp = response.toString()
                        Log.e("ResponseJsonText", resp)
                        val responseBase =
                            response.getJSONObject("Response")
                        val shouldClose = responseBase.optInt("ShouldClose", 1)
                        if (shouldClose == 0) {
                            if (resp.contains("IN-CORRECT ACTIVATION CODE") && state) {
                                Log.e(
                                    "Case",
                                    "Incorrect activation code||Deleted cache auth"
                                )
                                localStorage.cacheAuth = null
                            } else if (state) {
                                Log.e(
                                    "Case",
                                    "correct activation code||" + creditClubImage?.path
                                )
                                val auth = JSONObject()
                                auth.put("phone_number", authResponse.phoneNumber)
                                auth.put("session_id", authResponse.sessionId)
                                auth.put(
                                    "activation_code",
                                    creditClubImage?.path
                                )
                                localStorage.cacheAuth = auth.toString()
                            }
                            Misc.increaseTransactionMonitorCounter(
                                activity,
                                TransactionCountType.SUCCESS_COUNT,
                                authResponse.sessionId
                            )
                            if (resp.contains("MenuItem")) {
                                val menuWrapper =
                                    responseBase.getJSONObject("Menu")
                                        .getJSONObject("Response")
                                        .getJSONObject("Display")
                                requireActivity().supportFragmentManager
                                    .beginTransaction().replace(
                                        R.id.container,
                                        ListOptionsFragment.instantiate(
                                            menuWrapper,
                                            false
                                        )
                                    ).commit()
                            } else {
                                val menuWrapper =
                                    responseBase.getJSONObject("Menu")
                                        .getJSONObject("Response")["Display"]
                                if (menuWrapper is String && resp.contains("ShouldMask") && !resp.contains(
                                        "Invalid Response"
                                    )
                                ) {
                                    val data =
                                        responseBase.getJSONObject("Menu")
                                            .getJSONObject("Response")
                                    if (resp.contains("\"IsImage\":\"true\"")) {
                                        requireActivity().supportFragmentManager
                                            .beginTransaction().replace(
                                                R.id.container,
                                                instantiate(
                                                    data
                                                )
                                            ).commit()
                                    } else {
                                        requireActivity().supportFragmentManager
                                            .beginTransaction().replace(
                                                R.id.container,
                                                EnterDetailFragment.instantiate(data)
                                            ).commit()
                                    }
                                } else {
                                    val message =
                                        responseBase.getJSONObject("Menu")
                                            .getJSONObject("Response")
                                            .getString("Display")
                                    dialogProvider.showError(message)
                                }
                            }
                        } else {
                            Misc.increaseTransactionMonitorCounter(
                                activity,
                                TransactionCountType.ERROR_RESPONSE_COUNT,
                                authResponse.sessionId
                            )
                            if (responseBase.toString().contains("Display")) {
                                showDialogWithGoHomeAction(
                                    responseBase.getJSONObject("Menu")
                                        .getJSONObject("Response")
                                        .optString(
                                            "Display",
                                            ErrorMessages.OPERATION_NOT_COMPLETED
                                        )
                                )
                            } else {
                                dialogProvider.showError(
                                    responseBase.optString(
                                        "Menu",
                                        ErrorMessages.PHONE_NOT_REGISTERED
                                    )
                                )
                            }
                        }
                    }
                } catch (c: Exception) {
                    showDialogWithGoHomeAction("Something went wrong! Please try again.")
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.ERROR_RESPONSE_COUNT,
                        authResponse.sessionId
                    )
                }
            } else {
                if (e != null) {
                    if (e is TimeoutException) {
                        showDialogWithGoHomeAction("Something went wrong! Please try again.")
                        Misc.increaseTransactionMonitorCounter(
                            activity,
                            TransactionCountType.NO_INTERNET_COUNT,
                            authResponse.sessionId
                        )
                    } else {
                        dialogProvider.showError("Connection lost")
                        Misc.increaseTransactionMonitorCounter(
                            activity,
                            TransactionCountType.ERROR_RESPONSE_COUNT,
                            authResponse.sessionId
                        )
                    }
                } else {
                    dialogProvider.showError("Connection lost")
                    Misc.increaseTransactionMonitorCounter(
                        activity,
                        TransactionCountType.ERROR_RESPONSE_COUNT,
                        authResponse.sessionId
                    )
                }
            }
        }
    }

    companion object {

        @JvmStatic
        fun instantiate(data: JSONObject): CustomerImageFragment {
            return CustomerImageFragment().apply {
                optionsText = OptionsText(data)
            }
        }

        var state = false
    }
}