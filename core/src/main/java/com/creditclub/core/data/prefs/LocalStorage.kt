package com.creditclub.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.creditclub.core.data.model.AgentInfo
import com.creditclub.core.data.model.AuthResponse
import com.creditclub.core.util.delegates.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
class LocalStorage(
    context: Context,
    pref: SharedPreferences = context.getEncryptedSharedPreferences("agent_0")
) : SharedPreferences by pref {
    var cacheAuth: String? by valueStore(KEY_AUTH)
    var institutionCode: String? by valueStore(INSTITUTION_CODE)
    var agentPhone: String? by valueStore(AGENT_PHONE)
    var sessionID: String? by valueStore(SESSION_ID)
    var lastKnownLocation: String? by valueStore("LAST_KNOWN_LOCATION", "0.00;0.00")

    val authResponse: AuthResponse? by jsonStore(KEY_AUTH, AuthResponse.serializer(), defaultJson)
    var agent: AgentInfo? by jsonStore(AGENT_INFO, AgentInfo.serializer(), defaultJson)
    var transactionSequenceNumber: Long by longStore("transaction_sequence_number", 1)

    fun getString(key: String): String? = getString(key, null)

    fun putString(key: String, value: String?) = edit { putString(key, value) }

    companion object {
        private const val KEY_AUTH = "CACHE_AUTH_KEY"
        const val INSTITUTION_CODE = "INSTITUTION_CODE"
        const val AGENT_PHONE = "AGENT_PHONE"
        const val AGENT_INFO = "AGENT_INFO"
        const val SESSION_ID = "SESSION_ID"

        const val SuccessCount = "SUCCESS_COUNT"
        const val NoInternetCount = "NO_INTERNET_COUNT"
        const val NoResponseCount = "NO_RESPONSE_COUNT"
        const val ErrorResponseCount = "ERROR_RESPONSE_COUNT"
        const val RequestCount = "REQUEST_COUNT"
    }
}

fun LocalStorage.newTransactionReference(): String {
    return "${transactionSequenceNumber++}".padStart(8, '0')
}