package com.creditclub.pos

import androidx.activity.ComponentActivity
import org.json.JSONArray

interface PosParameter {
    var masterKey: String
    var sessionKey: String
    var pinKey: String
    var updatedAt: String?
    var managementDataString: String
    val managementData: ManagementData
    val capkList: JSONArray?
    val emvAidList: JSONArray?

    fun reset() {
        masterKey = ""
        sessionKey = ""
        pinKey = ""
        updatedAt = ""
    }

    suspend fun downloadCapk()
    suspend fun downloadAid()
    suspend fun downloadParameters()
    suspend fun downloadKeys()

    interface ManagementData {
        val cardAcceptorId: String
        val currencyCode: String
        val countryCode: String
        val merchantCategoryCode: String
        val cardAcceptorLocation: String
    }
}