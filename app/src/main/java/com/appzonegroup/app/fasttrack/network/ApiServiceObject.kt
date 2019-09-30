package com.appzonegroup.app.fasttrack.network

import android.util.Log
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.utility.Misc
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/1/2019.
 * Appzone Ltd
 */

object ApiServiceObject {
    private const val HOST = BuildConfig.API_HOST
    val BASE_URL = "$HOST/CreditClubMiddlewareAPI"
    const val CASE_LOG = "api/CaseLog"
    const val STATIC = "CreditClubStatic"
    const val PAY_BILLS = "api/PayBills"

    private val client: OkHttpClient by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger).build()
    }

    fun post(url: String, body: Any): ApiResult<String> {
        return post(url, Gson().toJson(body))
    }

    fun postAsync(url: String, body: Any, next: (ApiResult<String>) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.Default) {
                post(url, Gson().toJson(body))
            }

            next(result)
        }
    }

//    fun post(url: String, body: String, dialogProvider: DialogProvider? = null): String? {
//        log("URL: $url")
//        log("Request: $body")
//
//        dialogProvider?.activity?.runOnUiThread {
//            dialogProvider.showProgressBar("Loading...")
//        }
//
//        try {
//            val mediaType = MediaType.parse("application/json")
//
//            val requestBody = RequestBody.create(mediaType, body)
//            val request = Request.Builder()
//                    .url(url)
//                    .post(requestBody)
//                    .build()
//
//            log("Call Made" + Misc.getCurrentDateLongString())
//            val response = client.newCall(request).execute().body()?.string()
//            log("Response: $response")
//            return response// responseString;
//        } catch (ex: IOException) {
//            log("PostError: $ex")
//        } catch (ex: Exception) {
//            log("PostError: $ex")
//        } finally {
//            dialogProvider?.activity?.runOnUiThread {
//                dialogProvider.hideProgressBar()
//            }
//        }
//
//        return null
//    }

    @JvmOverloads
    fun post(url: String, body: String, headers: Headers = Headers.Builder().build()): ApiResult<String> {
        var response: String? = null
        var error: java.lang.Exception? = null

        try {
            val mediaType = MediaType.parse("application/json")

            val requestBody = RequestBody.create(mediaType, body)
            val request = Request.Builder()
                .url(url)
                .headers(headers)
                .post(requestBody)
                .build()

            Log.e("Call Made", Misc.getCurrentDateLongString())

            response = client.newCall(request).execute().body()!!.string()
            Log.e("RESPONSE:", response)
        } catch (ex: IOException) {
            Log.e("PostError", ex.toString())
            error = ex
        } catch (ex: Exception) {
            Log.e("PostError", ex.toString())
            error = ex
        } finally {
            return ApiResult(response, error)
        }
    }

//    fun <T> getList(url: String, dialogProvider: DialogProvider? = null): ArrayList<T>? {
//        val response = get(url, dialogProvider)
//
//        return Gson().fromJson(response, object : TypeToken<ArrayList<T>>() {}.type)
//    }

    @JvmOverloads
    fun get(url: String, bodyObject: Any? = null): ApiResult<String> {
        log("URL: $url")

        val body = when (bodyObject) {
            is String? -> bodyObject
            else -> Gson().toJson(bodyObject)
        }

        log("Request: $body")

        var response: String? = null
        var error: Exception? = null

        try {
            val mediaType = MediaType.parse("application/json")

            val requestBuilder = Request.Builder().url(url)

            if (body != null) {
                val requestBody = RequestBody.create(mediaType, body)
                requestBuilder.method("GET", requestBody)
            } else {
                requestBuilder.get()
            }

            val request = requestBuilder.build()

            log("Call Made" + Misc.getCurrentDateLongString())
            response = client.newCall(request).execute().body()?.string()
            log("Response: $response")
        } catch (ex: Exception) {
            log("GetError: $ex")
            error = ex
        } finally {
            return ApiResult(response, error)
        }
    }
//
//    fun get(url: String, dialogProvider: DialogProvider? = null): String? {
//        dialogProvider?.activity?.runOnUiThread {
//            dialogProvider.showProgressBar("Loading...")
//        }
//
//        try {
//            val request = Request.Builder().url(url).get().build()
//            Log.e("ApiServiceObject", "Call Made" + Misc.getCurrentDateLongString())
//
//            val response = client.newCall(request).execute().body()?.string()
//            Log.e("ApiServiceObject:", "Response: $response")
//            return response
//        } catch (ex: IOException) {
//            Log.e("GetError", ex.toString())
//            //  Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoInternetCount());
//        } catch (ex: Exception) {
//            Log.e("GetError", ex.toString())
//        } finally {
//            dialogProvider?.activity?.runOnUiThread {
//                dialogProvider.hideProgressBar()
//            }
//        }
//
//        return null
//    }

    fun log(text: String) {
        if (BuildConfig.DEBUG) {
            Log.d("ApiServiceObject", text)
        }
    }

    data class ApiResult<T>(val value: T?, val error: Exception?)
}