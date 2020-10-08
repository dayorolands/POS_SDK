package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PosNotificationResponse(
    @SerialName("Code")
    var code: String? = null,

    @SerialName("Message")
    var message: String? = null,

    @SerialName("BillerReference")
    var billerReference: String? = null
)