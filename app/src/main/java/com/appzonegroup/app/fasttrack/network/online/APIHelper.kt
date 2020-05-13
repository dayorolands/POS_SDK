package com.appzonegroup.app.fasttrack.network.online

import android.content.Context
import android.net.Uri
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.model.TransactionCountType
import com.appzonegroup.app.fasttrack.utility.Misc
import com.creditclub.core.data.CreditClubClient
import com.creditclub.core.data.Encryption
import com.creditclub.core.data.api.BankOneService
import com.creditclub.core.data.api.VolleyCompatibility
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import java.util.concurrent.TimeoutException


/**
 * @author fdamilola on 9/5/15.
 * @contact fdamilola@gmail.com +2348166200715
 */
class APIHelper(private val ctx: Context) {

    private val client by lazy { CreditClubClient(BuildConfig.API_HOST) }

    interface VolleyCallback<T> {
        fun onCompleted(e: Exception?, result: T?, status: Boolean)
    }

    fun attemptValidation(
        pNumber: String,
        sessionId: String,
        activationCode: String,
        location: String,
        state: Boolean,
        callback: VolleyCallback<String>
    )//, FutureCallback<String> futureCallback)
    {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val url = BankOneService.UrlGenerator.operationInit(
            pNumber,
            sessionId,
            activationCode,
            location,
            state,
            ctx.localStorage.institutionCode
        )

        val req = StringRequest(
            Request.Method.GET,
            url,
            Response.Listener { response -> callback.onCompleted(null, response, true) },
            Response.ErrorListener { error ->
                //                if (error.getCause() instanceof TimeoutException) {
                // Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.NO_INTERNET_COUNT, sessionId);
                //                }

                callback.onCompleted(error, null, false)
            })


        req.retryPolicy = DefaultRetryPolicy(
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleyCompatibility.processVolleyRequestWithOkHttp(
            req,
            Response.Listener { response -> callback.onCompleted(null, response, true) })
    }

    fun attemptActivation(
        pNumber: String,
        sessionId: String,
        activationCode: String,
        location: String,
        state: Boolean,
        callback: VolleyCallback<String>
    ) {

        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val url = BankOneService.UrlGenerator.operationActivation(
            pNumber,
            sessionId,
            activationCode,
            location,
            state,
            ctx.localStorage.institutionCode
        )
        val req = StringRequest(
            Request.Method.GET,
            url,
            Response.Listener { response -> callback.onCompleted(null, response, true) },
            Response.ErrorListener { error -> callback.onCompleted(error, null, false) })

        req.retryPolicy = DefaultRetryPolicy(
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleyCompatibility.processVolleyRequestWithOkHttp(
            req,
            Response.Listener { response -> callback.onCompleted(null, response, true) })
    }

    fun getNextOperation(
        pNumber: String,
        sessionId: String,
        next: String,
        location: String,
        callback: VolleyCallback<String>
    ) {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)

        val req = StringRequest(
            Request.Method.GET,
            BankOneService.UrlGenerator.operationNext(
                pNumber,
                sessionId,
                next,
                location,
                ctx.localStorage.institutionCode
            ),
            Response.Listener { response ->
                callback.onCompleted(null, response, true)
                //  if (response == null)
                //     Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);
            },
            Response.ErrorListener { error ->
                if (error.cause is TimeoutException) {
                    //       Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.NO_RESPONSE_COUNT, sessionId);
                }
                callback.onCompleted(error, null, false)
            })

        req.retryPolicy = DefaultRetryPolicy(
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )


        VolleyCompatibility.processVolleyRequestWithOkHttp(
            req,
            Response.Listener { response -> callback.onCompleted(null, response, true) })
    }

    fun getNextOperationImage(
        pNumber: String,
        sessionId: String,
        image: File,
        location: String,
        isFullImage: Boolean,
        callback: FutureCallback<String>
    ) {
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
//        Ion.with(ctx)
//            .load(
//                "POST",
//                BankOneService.UrlGenerator.operationNextImage(
//                    pNumber,
//                    sessionId,
//                    location,
//                    ctx.localStorage.institutionCode,
//                    isFullImage
//                )
//            )
//            .setTimeout(180000)
//            .setMultipartFile("file", image)
//            .asString().setCallback(callback)

        GlobalScope.launch(Dispatchers.Main) {
            val requestFile: RequestBody = RequestBody.create(
                MediaType.parse(ctx.contentResolver.getType(Uri.fromFile(image))),
                image
            )

            val body =
                MultipartBody.Part.createFormData("file", image.name, requestFile)

            val (response, error) = safeRunIO {
                client.bankOneService.operationNextImage(
                    pNumber,
                    sessionId,
                    Encryption.encrypt(location),
                    Encryption.encrypt(ctx.localStorage.institutionCode),
                    isFullImage,
                    body
                )
            }

            callback.onCompleted(error, response)
        }
    }

    fun continueNextOperation(
        pNumber: String,
        sessionId: String,
        next: String,
        location: String,
        callback: VolleyCallback<String>
    ) {
        //Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId);
        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val req = StringRequest(
            Request.Method.GET,
            BankOneService.UrlGenerator.operationContinue(
                pNumber,
                sessionId,
                next,
                location,
                ctx.localStorage.institutionCode
            ),
            Response.Listener { response ->
                if (response == null)
                //       Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.ERROR_RESPONSE_COUNT, sessionId);

                    callback.onCompleted(null, response, true)
            },
            Response.ErrorListener { error ->
                if (error.cause is TimeoutException) {
                    //             Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.NO_RESPONSE_COUNT, sessionId);
                }
                callback.onCompleted(error, null, false)
            })

        req.retryPolicy = DefaultRetryPolicy(
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleyCompatibility.processVolleyRequestWithOkHttp(
            req,
            Response.Listener { response -> callback.onCompleted(null, response, true) })
    }

//    fun continueNextOperationImage(
//        pNumber: String, sessionId: String, image: File, location: String,
//        callback: FutureCallback<String>
//    ) {
//        Ion.with(ctx)
//            .load(
//                "POST",
//                BankOneService.UrlGenerator.operationContinueImage(
//                    pNumber,
//                    sessionId,
//                    location,
//                    ctx.localStorage.institutionCode
//                )
//            )
//            .setTimeout(180000)
//            .setMultipartFile("file", image)
//            .asString().setCallback(callback)
//    }

    fun updateLocationAgent(
        number: String,
        longitude: String,
        latitude: String,
        callback: VolleyCallback<String>
    ) {

        val req = object : StringRequest(
            Request.Method.POST,
            BankOneService.UrlGenerator.BASE_URL_LOCATION,
            Response.Listener { response -> callback.onCompleted(null, response, true) },
            Response.ErrorListener { error -> callback.onCompleted(error, null, false) }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?> {

                val params = HashMap<String, String?>()
                params["OPERATION"] = Encryption.encrypt("GEO_LOCATION")
                params["AGENT_PHONE_NUMBER"] = Encryption.encrypt(number)
                params["LONGITUDE"] = Encryption.encrypt(longitude)
                params["LATITUDE"] = Encryption.encrypt(latitude)


                return params
            }
        }


        req.retryPolicy = DefaultRetryPolicy(
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleyCompatibility.processVolleyRequestWithOkHttp(
            req,
            Response.Listener { response -> callback.onCompleted(null, response, true) })
    }

    fun makeOfflineTransaction(
        sessionId: String, agentPhoneNumber: String,
        customerPhoneNumber: String, amount: String,
        agentPin: String, latitude: String, longitude: String,
        callback: VolleyCallback<String>
    ) {

        Misc.increaseTransactionMonitorCounter(ctx, TransactionCountType.REQUEST_COUNT, sessionId)
        val req = object : StringRequest(
            Request.Method.POST,
            BankOneService.UrlGenerator.BASE_URL_LOCATION,
            Response.Listener { response -> callback.onCompleted(null, response, true) },
            Response.ErrorListener { error -> callback.onCompleted(error, null, false) }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String?> {

                val params = HashMap<String, String?>()
                params["OPERATION"] = Encryption.encrypt("OFFLINE_DEPOSIT")
                params["AGENT_PHONE_NUMBER"] =
                    Encryption.encrypt(agentPhoneNumber.replace("234", "0"))
                params["CUSTOMER_PHONE_NUMBER"] = Encryption.encrypt(customerPhoneNumber)
                params["AMOUNT"] = Encryption.encrypt(amount)
                params["SESSION_ID"] = Encryption.encrypt(sessionId)
                params["AGENT_PIN"] = Encryption.encrypt(agentPin)
                params["LONGITUDE"] = Encryption.encrypt(longitude)
                params["LATITUDE"] = Encryption.encrypt(latitude)

                return params
            }
        }

        req.retryPolicy = DefaultRetryPolicy(
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleyCompatibility.processVolleyRequestWithOkHttp(
            req,
            Response.Listener { response -> callback.onCompleted(null, response, true) })
    }


    interface FutureCallback<T> {
        /**
         * onCompleted is called by the Future with the result or exception of the asynchronous operation.
         * @param e Exception encountered by the operation
         * @param result Result returned from the operation
         */
        fun onCompleted(e: Exception?, result: T?)
    }


    companion object {
        private const val MY_SOCKET_TIMEOUT_MS = 300000
    }
}
