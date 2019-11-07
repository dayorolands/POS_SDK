package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RequestStatus {

    @SerialName("Message")
    var message: String = ""

    @SerialName("Status")
    var status: Boolean = false
}
