package com.appzonegroup.app.fasttrack.network

import android.content.Context
import com.creditclub.core.util.safeRun
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

/**
 * Created by Joseph on 6/3/2016.
 */
object APICaller : KoinComponent {

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
            val client = get<OkHttpClient>(named("middleware"))
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
            val client = get<OkHttpClient>(named("middleware"))
            val request = Request.Builder()
                .url(urlString)
                .build()

            val response = client.newCall(request).execute()

            response.body?.string()
        }.data
    }
}