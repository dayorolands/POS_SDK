package com.creditclub.pos

import org.json.JSONArray

interface PosParameter {
    var masterKey: String
    var sessionKey: String
    var pinKey: String
    var managementDataString: String
    val managementData: ManagementData
    val capkList: JSONArray?
    val emvAidList: JSONArray?

    interface ManagementData {
        var cardAcceptorId: String
        var currencyCode: String
        var countryCode: String
        var merchantCategoryCode: String
        var cardAcceptorLocation: String
    }
}