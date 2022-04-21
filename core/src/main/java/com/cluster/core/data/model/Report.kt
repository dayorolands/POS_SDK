package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportResult<T>(
    @SerialName("Reports")
    val reports: List<T>? = null,

    @SerialName("totalCount")
    val totalCount: Int = 0
)