package com.cluster.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
open class MobileTrackingResponse {
    @SerialName("ResponseMessage")
    var responseMessage: String = ""
        get() = if (field.isEmpty()) message ?: "" else field

    @SerialName("isSuccessful")
    var isSuccessful: Boolean = false

    private val message: String? = null
}