package com.creditclub.pos

interface PosParameter {
    var masterKey: String
    var sessionKey: String
    var pinKey: String
    var managementDataString: String
    val managementData: ManagementData

    interface ManagementData {
        var cardAcceptorId: String
        var currencyCode: String
        var countryCode: String
        var merchantCategoryCode: String
        var cardAcceptorLocation: String
    }
}