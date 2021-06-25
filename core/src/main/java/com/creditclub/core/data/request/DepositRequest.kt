package com.creditclub.core.data.request

import com.creditclub.core.util.generateRRN
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
    val customerAccountNumber: String? = null,

    @SerialName("Amount")
    val amount: String? = null,

    @SerialName("AgentPin")
    val agentPin: String? = null,

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,

    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String? = generateRRN(),

    @SerialName("UniqueReferenceID")
    val uniqueReferenceID: String? = null,
)