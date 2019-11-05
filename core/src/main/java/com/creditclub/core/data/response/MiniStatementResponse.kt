package com.creditclub.core.data.response

import com.creditclub.core.data.model.MiniStatementTransaction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class MiniStatementResponse {

    @SerialName("MinistatementList")
    var data: List<MiniStatementTransaction>? = null

    @SerialName("ReponseMessage")
    var responseMessage: String? = null
        get() = if (field?.isEmpty() == true) message ?: "" else field

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean = false

    @SerialName("Message")
    private val message: String? = null
}
