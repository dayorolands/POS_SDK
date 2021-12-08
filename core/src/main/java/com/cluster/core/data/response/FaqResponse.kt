package com.cluster.core.data.response

import com.cluster.core.data.model.FaqItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FaqResponse {

    @SerialName("Data")
    var data: List<FaqItem>? = null

    @SerialName("ReponseMessage")
    var responseMessage: String? = null
        get() = if (field?.isEmpty() == true) message ?: "" else field

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean = false

    @SerialName("Message")
    private val message: String? = null
}
