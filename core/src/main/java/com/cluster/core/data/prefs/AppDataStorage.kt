package com.cluster.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.core.content.edit
import com.cluster.core.R
import com.cluster.core.data.model.AppVersion
import com.cluster.core.util.delegates.defaultJson
import com.cluster.core.util.delegates.jsonStore
import com.cluster.core.util.delegates.valueStore
import java.io.File

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
class AppDataStorage(
    context: Context,
    pref: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.APP_DATA_SOURCE),
        Context.MODE_PRIVATE
    ),
) : SharedPreferences by pref {
    var networkState: String? by valueStore("network_state")
    var networkCarrier: String? by valueStore("network_carrier")
    var deviceId: String? by valueStore("device_id")
    var latestVersion: AppVersion? by jsonStore(
        key = "LATEST_VERSION_JSON",
        json = defaultJson,
        serializer = AppVersion.serializer(),
    )
    private val otaAppName = context.getString(R.string.ota_app_name)
    private val downloadDir = context.getDownloadsFolder()
    val latestApkFileName: String?
        get() {
            val appVersion = latestVersion ?: return null
            return "$otaAppName${appVersion.version}.apk"
        }
    val latestApkFile: File?
        get() {
            val fileName = latestApkFileName ?: return null
            return File(downloadDir, fileName)
        }
    var updateDownloadId: Long by valueStore("update_download_id", -1)

    fun getString(key: String): String? = getString(key, null)

    fun putString(key: String, value: String?) = edit { putString(key, value) }

    companion object {
        private var INSTANCE: AppDataStorage? = null

        fun getInstance(context: Context): AppDataStorage {
            return INSTANCE ?: AppDataStorage(context).also { INSTANCE = it }
        }
    }
}

fun Context.getDownloadsFolder(): File {
    val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        ?: throw IllegalStateException("Failed to get external storage public directory")

    if (file.exists()) {
        if (!file.isDirectory) {
            throw IllegalStateException(
                (file.absolutePath
                        + " already exists and is not a directory")
            )
        }
    } else if (!file.mkdirs()) {
        throw IllegalStateException(
            ("Unable to create directory: "
                    + file.absolutePath)
        )
    }

    return file
}