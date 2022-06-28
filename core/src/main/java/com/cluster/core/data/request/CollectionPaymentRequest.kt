package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionPaymentRequest {
    @SerialName("PaymentReference")
    var paymentReference: String? = null

    @SerialName("PaymentMethod")
    var paymentMethod: Int? = null

    @SerialName("Channel")
    var channel: String? = null

    @SerialName("CustomerAccountName")
    var customerAcctName: String? = null

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = null

    @SerialName("CustomerName")
    var customerName: String? = null

    @SerialName("CustomerEmail")
    var customerEmail: String? = null

    @SerialName("BillerCode")
    var billerCode: String? = null

    @SerialName("BillerName")
    var billerName: String? = null

    @SerialName("BillerItemName")
    var billerItemName: String? = null

    @SerialName("ClientReference")
    var clientReference: String? = null

    @SerialName("PaymentGateway")
    var paymentGateway: String? = null

    @SerialName("BillerItemCode")
    var billerItemCode: String? = null

    @SerialName("Amount")
    var amount: Double? = null

    @SerialName("Currency")
    var currency: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("RequestReference")
    var requestReference: String? = null

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = null

    @SerialName("DeviceNumber")
    var deviceNumber: Int? = null

    @SerialName("ApplyFee")
    var applyFee: Boolean? = null

    @SerialName("FeeAmount")
    var feeAmount: Int? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

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