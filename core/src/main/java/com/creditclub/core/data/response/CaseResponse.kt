package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 09/09/2019.
 * Appzone Ltd
 */
@Serializable
data class CaseResponse<T>(
    @SerialName("Response")
    val response: T? = null,

    @SerialName("Status")
    val status: Boolean = false,

    @SerialName("IsResolved")
    val isResolved: Boolean = false,

    @SerialName("IsClosed")
    val isClosed: Boolean = false,
)