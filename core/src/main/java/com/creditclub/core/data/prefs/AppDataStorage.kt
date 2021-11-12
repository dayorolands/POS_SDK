package com.creditclub.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.creditclub.core.R
import com.creditclub.core.data.model.AppVersion
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.core.util.delegates.valueStore
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
    private val filesDirPath = context.filesDir.path
    private val latestApkFileName: String?
        get() {
            val appVersion = latestVersion ?: return null
            return "$otaAppName${appVersion.version}.apk"
        }
    val latestApkFile: File?
        get() {
            val fileName = latestApkFileName ?: return null
            val apkFolder = File(filesDirPath, "apks")
            if (!apkFolder.exists()) {
                apkFolder.mkdir()
            }
            val file = File("${apkFolder.path}/${fileName}")
            if (file.exists()) {
                file.delete()
            }
            return file
        }

    fun getString(key: String): String? = getString(key, null)

    fun putString(key: String, value: String?) = edit { putString(key, value) }

    companion object {
        private var INSTANCE: AppDataStorage? = null

        fun getInstance(context: Context): AppDataStorage {
            return INSTANCE ?: AppDataStorage(context).also { INSTANCE = it }
        }
    }
}