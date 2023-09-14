package com.cluster.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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