package com.cluster.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("Status")
    val status: String = "06",

    @SerialName("Message")
    val message: String? = null,

    @SerialName("Data")
    val data: T? = null,

    @SerialName("Code")
    val code: String = "00",
) {
    fun isSuccessful() = status == "00"
    fun isFailure() = status != "00"
    fun isUssdSuccess() = code == "00"

    companion object {
        const val Success = "00"
        const val Failed = "01"
        const val BadRequest = "03"
        const val ServerError = "04"
    }
}

val ApiResponse<*>?.isSuccessful: Boolean get() = this?.status == "00"