package com.cluster.pos

import org.json.JSONArray

const val DEFAULT_TRANSACTION_CURRENCY_CODE = "566"
const val DEFAULT_TERMINAL_COUNTRY_CODE = "566"

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