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

    fun reset()

    suspend fun downloadCapk(activity: ComponentActivity)
    suspend fun downloadAid(activity: ComponentActivity)
    suspend fun downloadParameters(activity: ComponentActivity)
    suspend fun downloadKeys(activity: ComponentActivity)

    interface ManagementData {
        var cardAcceptorId: String
        var currencyCode: String
        var countryCode: String
        var merchantCategoryCode: String
        var cardAcceptorLocation: String
    }
}