package com.cluster.core.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerValidationInfoResponse(
    @SerialName("Code")
    val code: String? = null,
    @SerialName("Data")
    val data: Data? = null,
    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false,
    @SerialName("Message")
    val message: String? = null,
    @SerialName("Status")
    val status: Int = 0,

)