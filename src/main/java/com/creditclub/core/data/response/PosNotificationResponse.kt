package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PosNotificationResponse {
    @SerialName("ResponseCode")
    var responseCode = ""

    @SerialName("ResponseMessage")
    var message = ""

    @SerialName("IsSuccessFul")
    var isSuccessFul = false

    @SerialName("Reference")
    var reference = ""

    @SerialName("RequestStatus")
    var requestStatus = false

    @SerialName("ResponseDescription")
    var responseDescription = ""

    @SerialName("ResponseStatus")
    var responseStatus = ""

    @SerialName("BillerReference")
    var billerReference: String? = null
}