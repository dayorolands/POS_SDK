package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WithdrawalRequest(

    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String?,

    @SerialName("InstitutionCode")
    val institutionCode: String?,

    @SerialName("CustomerAccountNumber")
    val customerAccountNumber: String,

    @SerialName("AgentPin")
    val agentPin: String? = null,

    @SerialName("Token")
    val token: String? = null,

    @SerialName("CustomerPin")
    val customerPin: String? = null,

    @SerialName("Amount")
    val amount: String,

    @SerialName("GeoLocation")
    val geoLocation: String? = null,

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,

    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String,

    @SerialName("DeviceNumber")
    val deviceNumber: Int = 0,
) {
    @Serializable
    data class Additional(
        @SerialName("Currency")
        val currency: String? = "",

        @SerialName("CustomerPhoneNumber")
        val customerPhoneNumber: String? = null,
    )
}