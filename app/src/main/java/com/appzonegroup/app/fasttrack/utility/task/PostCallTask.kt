package com.appzonegroup.app.fasttrack.utility.task

import android.app.Activity
import android.os.AsyncTask
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.safeRun
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named

class PostCallTask(
    var dialogProvider: DialogProvider,
    activity: Activity,
    asyncResponse: AsyncResponse?
) : AsyncTask<String, Void?, String?>() {
    var delegate: AsyncResponse? = asyncResponse
    private val koin = GlobalContext.get().koin

    override fun onPreExecute() {
        super.onPreExecute()
        dialogProvider.showProgressBar("Loading")
    }

    override fun onPostExecute(s: String?) {
        super.onPostExecute(s)
        dialogProvider.hideProgressBar()
        delegate?.processFinished(s)
    }

    override fun doInBackground(vararg params: String): String? {
        val url = params[0]
        val parameter = params[1]
        return safeRun {
            val mediaType = "application/json".toMediaTypeOrNull()
            val client = koin.get<OkHttpClient>(named("middleware"))
            val body = parameter.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            response.body?.string()
        }.data
    }
}