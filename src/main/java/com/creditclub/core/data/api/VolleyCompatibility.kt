package com.creditclub.core.data.api

import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.creditclub.core.BuildConfig
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/20/2019.
 * Appzone Ltd
 */

object VolleyCompatibility {
    private val client by lazy {
        OkHttpClient().newBuilder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    @JvmStatic
    fun processVolleyRequestWithOkHttp(req: StringRequest, listener: com.android.volley.Response.Listener<String>) {
//        val mediaType = MediaType.parse("text/plain")

        GlobalScope.launch(Dispatchers.Main) {
            val newHeaders = Headers.Builder()

            req.headers.forEach { (name, value) -> newHeaders.add(name, value) }

            val requestBody = if (req.body != null) {
                RequestBody.create(null, req.body)
            } else {
                RequestBody.create(null, "{}")
            }

            val requestBuilder = Request.Builder()
                .url(req.url)
                .headers(newHeaders.build())

            if (req.method == com.android.volley.Request.Method.POST) requestBuilder.post(requestBody)

            val request = requestBuilder.build()

            val (response, error) = safeRunIO {
                client.newCall(request).execute()
            }

            if (error != null) req.errorListener.onErrorResponse(VolleyError(error))
            else listener.onResponse(response?.body()?.string())
        }
    }
}

