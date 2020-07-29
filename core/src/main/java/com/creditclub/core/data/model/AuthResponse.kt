package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
class AuthResponse {
    @SerialName("phone_number")
    var phoneNumber: String? = null
        private set

    @SerialName("session_id")
    var sessionId: String? = null

    @SerialName("activationCode")
    var activationCode: String? = null

    constructor(phoneNumber: String, activationCode: String) {
        this.phoneNumber = phoneNumber
        this.activationCode = activationCode
    }

    constructor(js: JSONObject) {
        phoneNumber = js.optString("phone_number")
        sessionId = js.optString("session_id")
        activationCode = js.optString("activationCode")
    }
}
