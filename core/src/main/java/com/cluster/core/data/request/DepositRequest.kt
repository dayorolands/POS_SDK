package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DepositRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,

    @SerialName("GeoLocation")
    val geoLocation: String? = null,

    @SerialName("CustomerAccountNumber")
    val customerAccountNumber: String,

    @SerialName("Amount")
    val amount: String,

    @SerialName("AgentPin")
    val agentPin: String? = null,

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,

    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String,

    @SerialName("DeviceNumber")
    val deviceNumber: Int = 0,

    @SerialName("UniqueReferenceID")
    val uniqueReferenceID: String? = null,
)