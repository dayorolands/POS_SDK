package com.appzonegroup.app.fasttrack.network

import android.content.Context
import com.appzonegroup.app.fasttrack.BuildConfig
import com.creditclub.core.util.safeRun
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Created by Joseph on 6/3/2016.
 */
object APICaller {

    @JvmStatic
    @JvmOverloads
    fun postRequest(
        context: Context?,
        url: String,
        json: String,
        token: String? = null
    ): String? {
        return safeRun {
            val mediaType = "application/json".toMediaTypeOrNull()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .addInterceptor(interceptor)
                .readTimeout(5, TimeUnit.MINUTES)
                .build()
            val body = json.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body).also {
                    if (token != null) {
                        it.header("AuthToken", token)
                    }
                }
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()
        }.data
    }

    @JvmStatic
    fun makeGetRequest2(urlString: String): String? {
        return safeRun {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .addInterceptor(interceptor)
                .readTimeout(5, TimeUnit.MINUTES)
                .build()

            val request = Request.Builder()
                .url(urlString)
                .build()

            val response = client.newCall(request).execute()

            response.body?.string()
        }.data
    }
}