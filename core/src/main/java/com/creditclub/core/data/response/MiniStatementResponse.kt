package com.creditclub.core.data.response

import com.creditclub.core.data.model.MiniStatementTransaction
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
