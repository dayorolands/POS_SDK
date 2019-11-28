package com.creditclub.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.creditclub.core.R
import com.creditclub.core.data.model.AppVersion
import com.creditclub.core.util.delegates.valueStore
import kotlinx.serialization.json.Json

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
class AppDataStorage(
    context: Context,
    pref: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.APP_DATA_SOURCE),
        Context.MODE_PRIVATE
    )
) : SharedPreferences by pref {

    private var latestVersionJson: String? by valueStore("LATEST_VERSION_JSON")
    var latestVersionLink: String? by valueStore("LATEST_VERSION_LINK")

    var latestVersion: AppVersion?
        get() = if (latestVersionJson != null) {
            Json.nonstrict.parse(
                AppVersion.serializer(),
                latestVersionJson!!
            )
        } else null
        set(value) {
            latestVersionJson = if (value != null) {
                Json.stringify(AppVersion.serializer(), value)
            } else null
        }

    fun getString(key: String): String? = getString(key, null)

    fun putString(key: String, value: String?) = edit { putString(key, value) }

    companion object {

        const val SuccessCount = "SUCCESS_COUNT"
        const val NoInternetCount = "NO_INTERNET_COUNT"
        const val NoResponseCount = "NO_RESPONSE_COUNT"
        const val ErrorResponseCount = "ERROR_RESPONSE_COUNT"
        const val RequestCount = "REQUEST_COUNT"

        private var INSTANCE: AppDataStorage? = null

        fun getInstance(context: Context): AppDataStorage {
            return INSTANCE ?: AppDataStorage(context).also { INSTANCE = it }
        }
    }
}