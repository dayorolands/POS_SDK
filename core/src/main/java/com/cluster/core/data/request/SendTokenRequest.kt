package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SendTokenRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("CustomerAccountNumber")
    val customerAccountNumber: String? = null,

    @SerialName("CustomerPhoneNumber")
    val customerPhoneNumber: String? = null,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,

    @SerialName("AgentPin")
    val agentPin: String? = null,

    @SerialName("Amount")
    val amount: Double = 0.0,

    @SerialName("OperationType")
    val operationType: String? = null,

    @SerialName("ReferenceNumber")
    val referenceNumber: String? = null,

    @SerialName("IsPinChange")
    val isPinChange: Boolean = false,
)