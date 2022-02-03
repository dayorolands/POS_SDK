package com.cluster.core.data.response

import com.cluster.core.data.model.MiniStatementTransaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MiniStatementResponse(
    @SerialName("MinistatementList")
    val data: List<MiniStatementTransaction>? = null,

    @SerialName("ReponseMessage")
    val responseMessage: String? = null,

    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false,
)
