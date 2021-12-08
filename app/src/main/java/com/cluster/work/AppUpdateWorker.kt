package com.cluster.work

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cluster.R
import com.appzonegroup.creditclub.pos.Platform
import com.cluster.core.data.api.AppConfig
import com.cluster.core.data.api.VersionService
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.ui.getLatestVersion
import com.cluster.core.util.packageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AppUpdateWorker(
    context: Context,
    params: WorkerParameters,
    private val appDataStorage: AppDataStorage,
    private val localStorage: LocalStorage,
    private val versionService: VersionService,
    private val appConfig: AppConfig,
) : CoroutineWorker(context, params) {

    private val appName = context.getString(R.string.app_name)

    override suspend fun doWork(): Result {
        val result = getLatestVersion(
            versionService = versionService,
            appDataStorage = appDataStorage,
            appConfig = appConfig,
            localStorage = localStorage,
            deviceType = Platform.deviceType,
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

        val latestApkFileUri = latestApkFile.toUri()
        val manager =
            applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val oldUpdateDownloadId = appDataStorage.updateDownloadId
        if (oldUpdateDownloadId > 0) {
            val oldFileUri: Uri? = manager.getUriForDownloadedFile(appDataStorage.updateDownloadId)
            if (oldFileUri == null || oldFileUri != latestApkFileUri) {
                manager.remove(oldUpdateDownloadId)
            } else {
                return Result.success()
            }
        }

        val request = DownloadManager.Request(latestVersion.link.toUri()).apply {
            setDescription("Downloading ${latestVersion.version}")
            setTitle("$appName update")
            setAllowedOverMetered(true)
            setMimeType("application/vnd.android.package-archive")
            setAllowedOverRoaming(true)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                appDataStorage.latestApkFileName
            )
        }
        appDataStorage.updateDownloadId = manager.enqueue(request)

        return Result.success()
    }
}