package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/26/2019.
 * Appzone Ltd
 */

@Serializable
data class GenericResponse<T>(
    @SerialName("ResponseCode")
    val responseCode: String = "",

    @SerialName("ResponseMessage")
    val responseMessage: String = "",

    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false,

    @SerialName("Data")
    val data: T? = null,
)