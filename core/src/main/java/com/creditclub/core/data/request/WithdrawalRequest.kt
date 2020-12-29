package com.creditclub.core.data.request

import com.creditclub.core.util.generateRRN
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class WithdrawalRequest {

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("Token")
    var token: String? = null

    @SerialName("CustomerPin")
    var customerPin: String? = null

    @SerialName("Amount")
    var amount: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = generateRRN()

    @Serializable
    data class Additional(
        @SerialName("Currency")
        var currency: String? = "",

        @SerialName("CustomerPhoneNumber")
        var customerPhoneNumber: String? = null,
    )
}