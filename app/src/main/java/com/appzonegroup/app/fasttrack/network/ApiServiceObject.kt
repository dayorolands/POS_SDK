package com.appzonegroup.app.fasttrack.network

import android.util.Log
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.utility.Misc
import com.creditclub.core.util.debugOnly
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/1/2019.
 * Appzone Ltd
 */

object ApiServiceObject {
    private const val HOST = BuildConfig.API_HOST
    const val BASE_URL = "$HOST/CreditClubMiddlewareAPI"
    const val CASE_LOG = "api/CaseLog"

    private val client: OkHttpClient by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.SECONDS)
            .addInterceptor(logger).build()
    }

    fun postAsync(url: String, body: Any, next: (ApiResult<String>) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.Default) {
                post(url, Gson().toJson(body))
            }

            next(result)
        }
    }

    @JvmOverloads
    fun post(
        url: String,
        body: String,
        headers: Headers = Headers.Builder().build()
    ): ApiResult<String> {
        var response: String? = null
        var error: java.lang.Exception? = null

        try {
            val mediaType = "application/json".toMediaTypeOrNull()

            val requestBody = body.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .headers(headers)
                .post(requestBody)
                .build()

            Log.e("Call Made", Misc.getCurrentDateLongString())

            response = client.newCall(request).execute().body!!.string()
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
            val mediaType = "application/json".toMediaTypeOrNull()

            val requestBuilder = Request.Builder().url(url)

            if (body != null) {
                val requestBody = body.toRequestBody(mediaType)
                requestBuilder.method("GET", requestBody)
            } else {
                requestBuilder.get()
            }

            val request = requestBuilder.build()

            log("Call Made" + Misc.getCurrentDateLongString())
            response = client.newCall(request).execute().body?.string()
            log("Response: $response")
        } catch (ex: Exception) {
            log("GetError: $ex")
            error = ex
        } finally {
            return ApiResult(response, error)
        }
    }

    fun log(text: String) = debugOnly {
        Log.d("ApiServiceObject", text)
    }

    data class ApiResult<T>(val value: T?, val error: Exception?)
}