package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionReferenceGenerationRequest {
    @SerialName("Amount")
    var amount: Double? = null

    @SerialName("Currency")
    var currency: String? = null

    @SerialName("Reference")
    var reference: String? = null

    @SerialName("CustomerID")
    var customerId: String? = null

    @SerialName("PhoneNumber")
    var phoneNumber: String? = null

    @SerialName("CategoryCode")
    var categoryCode: String? = null

    @SerialName("ItemCode")
    var itemCode: String? = null

    @SerialName("ReferenceName")
    var referenceName: String? = null

    @SerialName("CollectionService")
    var collectionService: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("Region")
    var region: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("RequestReference")
    var requestReference: String? = null

    @SerialName("ApplyFee")
    var applyFee: Boolean? = null

    @SerialName("FeeAmount")
    var feeAmount: Double? = null

    @SerialName("FeeBearerAccount")
    var feeBearerAccount: String? = null

    @SerialName("FeeSuspenseAccount")
    var feeSuspenseAccount: String? = null

    @Serializable
    class Additional {
        @SerialName("TerminalID")
        var terminalId: String? = null

        @SerialName("AgentCode")
        var agentCode: String? = null
    }
}