package com.appzonegroup.creditclub.pos.service

import android.util.Log
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.util.Misc
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/24/2019.
 * Appzone Ltd
 */

object ApiService {
    private const val HOST = BuildConfig.API_HOST
    const val BASE_URL = "$HOST/CreditClubMiddlewareAPI/CreditClubStatic"

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

    @JvmOverloads
    fun post(url: String, body: String, headers: Headers = Headers.Builder().build()): Result<String> {
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

            response = client.newCall(request).execute().body()!!.string()
        } catch (ex: IOException) {
            error = ex
        } catch (ex: Exception) {
            error = ex
        } finally {
            return Result(response, error)
        }
    }

    fun <T> getList(url: String, clazz: Class<T>): List<T>? {
        return Gson().fromJson(getRaw(url), object : TypeToken<List<T>>() {}.type)
    }

    fun <T> get(url: String, clazz: Class<T>): T {
        return Gson().fromJson(getRaw(url), clazz)
    }

    fun <T> get(url: String, clazz: Class<T>, next: (T?) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            var response: T? = null

            try {
                response = withContext(Dispatchers.Default) {
                    val apiResponse: String? = getRaw(url)
                    Gson().fromJson(apiResponse, clazz)
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            } finally {
                next(response)
            }
        }
    }

    fun get(url: String, next: (Result<String>) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val response = withContext(Dispatchers.Default) {
                get(url)
            }
            next(response)
        }
    }

    fun get(url: String): Result<String> {
        var response: String? = null
        var error: java.lang.Exception? = null

        try {
            Log.e("Call", url)

            val request = Request.Builder().url(url)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .get()
                .build()

            Log.e("Call Made", Misc.getCurrentDateLongString())

            response = client.newCall(request).execute().body()!!.string()

            Log.e("RESPONSE:", response)
        } catch (ex: IOException) {
            Log.e("GetError", ex.toString())
            error = ex
            //  Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoInternetCount());
        } catch (ex: Exception) {
            Log.e("GetError", ex.toString())
            error = ex
//            val exceptionMessage = ex.toString()
//            if (exceptionMessage.contains("TimeOut")) {
//                // Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoResponseCount());
//            } else if (exceptionMessage.contains("\"Status\":false")) {
//                //Misc.increaseTransactionMonitorCounter(context, AppConstants.getErrorResponseCount());
//            } else {
//                //Misc.increaseTransactionMonitorCounter(context, AppConstants.getNoInternetCount());
//            }
        } finally {
            return Result(response, error)
        }
    }

    private fun getRaw(url: String): String? {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
            .readTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
            .build()

        val request = Request.Builder().url(url)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .get()
            .build()

        Log.e("Call Made", Misc.getCurrentDateLongString())

        return client.newCall(request).execute().body()!!.string()
    }

    data class Result<T>(val value: T?, val error: java.lang.Exception?)
}