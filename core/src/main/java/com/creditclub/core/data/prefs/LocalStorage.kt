package com.creditclub.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.creditclub.core.R
import com.creditclub.core.data.model.AgentInfo
import com.creditclub.core.data.model.AuthResponse
import com.creditclub.core.util.delegates.valueStore
import kotlinx.serialization.json.Json

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
class LocalStorage private constructor(
    context: Context,
    pref: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.DATA_SOURCE),
        Context.MODE_PRIVATE
    )
) : SharedPreferences by pref {
    var cacheAuth: String? by valueStore(KEY_AUTH)
    var institutionCode: String? by valueStore(INSTITUTION_CODE)
    var agentPhone: String? by valueStore(AGENT_PHONE)
    var agentPIN: String? by valueStore(AGENT_PIN)
    var agentInfo: String? by valueStore(AGENT_INFO)
    var sessionID: String? by valueStore(SESSION_ID)
    val agentIsActivated: Boolean
        get() = getString("ACTIVATED") != null

    val isLoggedIn get() = cacheAuth != null

    fun deleteCacheAuth() = edit { clear() }

    val authResponse: AuthResponse?
        get() {
            val cacheAuth = cacheAuth ?: return null

            return try {
                Json.parse(AuthResponse.serializer(), cacheAuth)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    var agent: AgentInfo? = null
        get() {
            if (field != null) return field

            val agentInfo = agentInfo ?: return null

            return try {
                Json.nonstrict.parse(AgentInfo.serializer(), agentInfo)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        set(value) {
            value ?: return run {
                agentInfo = null
                field = null
            }

            try {
                agentInfo = Json.nonstrict.stringify(AgentInfo.serializer(), value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    fun getString(key: String): String? = getString(key, null)

    fun putString(key: String, value: String?) = edit { putString(key, value) }

    companion object {
        private const val KEY_AUTH = "CACHE_AUTH_KEY"
        const val INSTITUTION_CODE = "INSTITUTION_CODE"
        const val AGENT_PIN = "AGENT_PIN"
        const val AGENT_PHONE = "AGENT_PHONE"
        const val AGENT_CODE = "AGENT_CODE"
        const val AGENT_INFO = "AGENT_INFO"
        const val AGENT_NAME = "AGENT_NAME"
        const val SESSION_ID = "SESSION_ID"

        const val SuccessCount = "SUCCESS_COUNT"
        const val NoInternetCount = "NO_INTERNET_COUNT"
        const val NoResponseCount = "NO_RESPONSE_COUNT"
        const val ErrorResponseCount = "ERROR_RESPONSE_COUNT"
        const val RequestCount = "REQUEST_COUNT"

        private var INSTANCE: LocalStorage? = null

        fun getInstance(context: Context): LocalStorage {
            return INSTANCE ?: LocalStorage(context.applicationContext).also { INSTANCE = it }
        }
    }
}