package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CaseMessageThreadRequest(
    @SerialName("CaseReference")
    val caseReference: String,
)