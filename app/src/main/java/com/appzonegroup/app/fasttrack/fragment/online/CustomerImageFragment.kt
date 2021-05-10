package com.appzonegroup.app.fasttrack.fragment.online

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.appzonegroup.app.fasttrack.BankOneApplication
import com.appzonegroup.app.fasttrack.OnlineActivity
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentCustomerImageBinding
import com.appzonegroup.app.fasttrack.fragment.online.EnterDetailFragment.OptionsText
import com.appzonegroup.app.fasttrack.model.TransactionCountType
import com.appzonegroup.app.fasttrack.model.online.Response
import com.appzonegroup.app.fasttrack.network.online.APIHelper
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.GPSTracker
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages
import com.appzonegroup.app.fasttrack.utility.online.convertXmlToJson
import com.creditclub.core.data.Encryption
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.safeRunIO
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeoutException

class CustomerImageFragment : CreditClubFragment(R.layout.fragment_customer_image),
    View.OnClickListener {
    private var creditClubImage: CreditClubImage? = null
    private var image: Bitmap? = null
    private val binding by dataBinding<FragmentCustomerImageBinding>()
    private var optionsText: OptionsText? = null

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

    private fun openCamera() {
        ImagePicker.cameraOnly().start(this)
    }

    private fun openGallery() {
        ImagePicker.create(this).returnMode(ReturnMode.ALL)
            .folderMode(true)
            .single().single().showCamera(false).start()
    }

    var finalLocation: String? = null

    override fun onClick(view: View) {
        try {
            if (view === binding.gallery) {
                openGallery()
            } else if (view === binding.takePhoto) {
                openCamera()
            } else {

                if (image != null) {
                    //final CacheHelper ch = new CacheHelper(getActivity());
                    finalLocation = gps.geolocationString
                    mainScope.launch { nextOperation() }

                } else {
                    Toast.makeText(
                        activity,
                        "Please select or take a picture!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (ex: Exception) {
            Toast.makeText(
                activity,
                "An error just occurred. Please try again later.",
                Toast.LENGTH_LONG
            ).show()
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
            finalLocation ?: "",
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
        //final AuthResponse authResponse = bankOneApplication.getAuthResponse();// LocalStorage.getCachedAuthResponse(getActivity());
        val authResponse =
            (requireActivity().application as BankOneApplication).authResponse
        val ah = APIHelper(requireActivity())
        var finalLocation = "0.00;0.00"
        val gpsTracker = GPSTracker(activity)
        if (gpsTracker.location != null) {
            val longitude = gpsTracker.location.longitude.toString()
            val latitude = gpsTracker.location.latitude.toString()
            finalLocation = "$latitude;$longitude"
        }
        ah.getNextOperation(authResponse.phoneNumber,
            authResponse.sessionId,
            creditClubImage?.path ?: "",
            finalLocation
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
                        if (responseBase != null) {
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            try {
                val tmpImage: Image? = ImagePicker.getFirstImageOrNull(data)
                tmpImage ?: return dialogProvider.showError("An internal error occurred")
                creditClubImage = CreditClubImage(requireContext(), tmpImage)

                mainScope.launch {
                    dialogProvider.showProgressBar("Processing image")

                    val (bitmap) = safeRunIO { creditClubImage?.bitmap }
                    image = bitmap
                    binding.image.setImageBitmap(bitmap)

                    dialogProvider.hideProgressBar()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                dialogProvider.showError("An internal error occurred")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {

        @JvmStatic
        fun instantiate(data: JSONObject?): CustomerImageFragment {
            return CustomerImageFragment().apply {
                optionsText = OptionsText(data)
            }
        }

        var state = false
    }
}