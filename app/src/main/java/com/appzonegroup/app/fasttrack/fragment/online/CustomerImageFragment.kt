package com.appzonegroup.app.fasttrack.fragment.online

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appzonegroup.app.fasttrack.BankOneApplication
import com.appzonegroup.app.fasttrack.BaseActivity
import com.appzonegroup.app.fasttrack.OnlineActivity
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.fragment.online.EnterDetailFragment.OptionsText
import com.appzonegroup.app.fasttrack.model.TransactionCountType
import com.appzonegroup.app.fasttrack.model.online.Response
import com.appzonegroup.app.fasttrack.network.online.APIHelper
import com.appzonegroup.app.fasttrack.network.online.APIHelper.VolleyCallback
import com.appzonegroup.app.fasttrack.utility.GPSTracker
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages
import com.appzonegroup.app.fasttrack.utility.online.XmlToJson
import com.creditclub.core.data.Encryption
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.safeRunIO
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeoutException

class CustomerImageFragment : Fragment(),
    View.OnClickListener {
    private var creditClubImage: CreditClubImage? = null
    private var imageString: String? = null
    private var image: Bitmap? = null

    val next: Button? by lazy { view?.findViewById<Button>(R.id.btnActivate) }
    private val fromGallery: Button? by lazy { view?.findViewById<Button>(R.id.gallery) }
    private val takePhoto: Button? by lazy { view?.findViewById<Button>(R.id.takePhoto) }
    private val upperHint: TextView? by lazy { view?.findViewById<TextView>(R.id.upperHint) }
    private val imageView: ImageView? by lazy { view?.findViewById<ImageView>(R.id.image) }
    private val bankOneApplication: BankOneApplication? by lazy {
        activity?.application as BankOneApplication?
    }
    var optionsText: OptionsText? = null

    private val baseActivity: BaseActivity by lazy { activity as BaseActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            inflater.inflate(R.layout.fragment_customer_image, container, false)
        OnlineActivity.isHome = false
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        upperHint!!.text = if (optionsText == null) "" else optionsText!!.hintText
        fromGallery!!.setOnClickListener(this)
        takePhoto!!.setOnClickListener(this)
        next!!.setOnClickListener(this)

        activity?.currentFocus?.let { view ->
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
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
            if (view === fromGallery) {
                openGallery()
            } else if (view === takePhoto) {
                openCamera()
            } else {

                if (image != null) {
                    //final CacheHelper ch = new CacheHelper(getActivity());
                    finalLocation = "0.00;0.00"
                    val gpsTracker = GPSTracker(activity)
                    if (gpsTracker.location != null) {
                        Log.e("CangetLocation", "NULL")
                        val longitude = gpsTracker.location.longitude.toString()
                        val latitude = gpsTracker.location.latitude.toString()
                        finalLocation = "$latitude;$longitude"
                        Log.e("Location", finalLocation)
                    }
                    nextOperation()

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

    private fun nextOperation() {
        val finalFile = File(creditClubImage?.path)
        val authResponse = bankOneApplication?.authResponse
        val ah = APIHelper(requireActivity())
        baseActivity.showProgressBar("Uploading")
        ah.getNextOperationImage(authResponse?.phoneNumber ?: "",
            authResponse?.sessionId ?: "nothing",
            finalFile,
            finalLocation ?: "",
            optionsText?.isShouldCompress ?: false,
            baseActivity.mainScope,
            object : APIHelper.FutureCallback<String> {
                override fun onCompleted(e: Exception?, result: String?) {
                    baseActivity.hideProgressBar()
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
                                baseActivity.showError("Connection lost")
                            }
                        } else {
                            baseActivity.showError("Connection lost")
                            Misc.increaseTransactionMonitorCounter(
                                activity,
                                TransactionCountType.NO_INTERNET_COUNT,
                                authResponse?.sessionId
                            )
                        }
                    }
                }
            })
    }

    fun showDialogWithGoHomeAction(message: String?) {
        baseActivity.showError<Nothing>(message) {
            onClose {
                (activity as OnlineActivity?)?.goHome()
            }
        }
    }

    private fun moveToNext() {
        baseActivity.showProgressBar("Loading")
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
            finalLocation,
            object : VolleyCallback<String> {
                override fun onCompleted(
                    e: Exception?,
                    result: String?,
                    status: Boolean
                ) {
                    baseActivity.hideProgressBar()
                    if (status) {
                        try {
                            val answer = Response.fixResponse(result)
                            Log.e("DECRYPTED", Encryption.decrypt(answer))
                            val decryptedAnswer = Encryption.decrypt(answer)
                            val response =
                                XmlToJson.convertXmlToJson(decryptedAnswer)
                            if (response == null) {
                                baseActivity.showError("Connection lost")
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
                                            LocalStorage.deleteCacheAuth(activity)
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
                                            LocalStorage.saveCacheAuth(
                                                auth.toString(),
                                                activity
                                            )
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
                                                if (resp.contains("\"IsImage\":true")) {
                                                    activity!!.supportFragmentManager
                                                        .beginTransaction().replace(
                                                            R.id.container,
                                                            instantiate(
                                                                data
                                                            )
                                                        ).commit()
                                                } else {
                                                    activity!!.supportFragmentManager
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
                                                baseActivity.showError(message)
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
                                            baseActivity.showError(
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
                                baseActivity.showError("Connection lost")
                                Misc.increaseTransactionMonitorCounter(
                                    activity,
                                    TransactionCountType.ERROR_RESPONSE_COUNT,
                                    authResponse.sessionId
                                )
                            }
                        } else {
                            baseActivity.showError("Connection lost")
                            Misc.increaseTransactionMonitorCounter(
                                activity,
                                TransactionCountType.ERROR_RESPONSE_COUNT,
                                authResponse.sessionId
                            )
                        }
                    }
                }
            })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            (activity as BaseActivity).run {
                try {
                    val tmpImage: Image? = ImagePicker.getFirstImageOrNull(data)
                    tmpImage ?: return@run showError("An internal error occurred")
                    creditClubImage = CreditClubImage(tmpImage)

                    baseActivity.mainScope.launch {
                        showProgressBar("Processing image")

                        val (bitmap) = safeRunIO { creditClubImage?.bitmap }
                        image = bitmap
                        imageView?.setImageBitmap(bitmap)

                        imageString = safeRunIO { creditClubImage?.bitmapString }.data
                        hideProgressBar()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    showError("An internal error occurred")
                }
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