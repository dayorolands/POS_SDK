package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.api.VersionService
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.ui.getLatestVersion
import com.creditclub.core.util.packageInfo
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.buffer
import okio.sink
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AppUpdateWorker(
    context: Context,
    params: WorkerParameters,
    private val appDataStorage: AppDataStorage,
    private val okHttpClient: OkHttpClient,
    private val versionService: VersionService,
    private val appConfig: AppConfig,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = getLatestVersion(
            versionService = versionService,
            appConfig = appConfig,
            appDataStorage = appDataStorage,
        )
        if (result.isFailure) return Result.retry()
        val latestVersion = appDataStorage.latestVersion ?: return Result.retry()
        val currentVersion = applicationContext.packageInfo!!.versionName
        if (!latestVersion.isNewerThan(currentVersion)) {
            return Result.success()
        }
        val latestApkFile = appDataStorage.latestApkFile ?: return Result.retry()
        val exists = withContext(Dispatchers.IO) { latestApkFile.exists() }
        if (exists) {
            return Result.success()
        }
        val url = latestVersion.link

        val response = suspendCoroutine<Response?> { continuation ->
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    continuation.resume(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }
            })
        }

        if (response?.body == null) {
            return Result.retry()
        }

        return withContext(Dispatchers.IO) {
            latestApkFile.createNewFile()
            latestApkFile.sink().buffer().use { sink ->
                sink.writeAll(response.body!!.source())
            }

            Result.success()
        }
    }
}