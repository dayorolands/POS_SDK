package com.cluster.core.data.model

import com.cluster.core.data.response.BackendResponseContract
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetBankResponse {
    @SerialName("Data")
    val data: List<Data>? = null
    @SerialName("Message")
    val responseMessage: String? = null
    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false
    @SerialName("Code")
    val code: String =""
    @SerialName("Status")
    val status: Int = 0

    @Serializable
    class Data{
        @SerialName("Name")
        val name: String = ""
        @SerialName("ShortName")
        val shortName: String = ""
        @SerialName("Code")
        val dataCode: String = ""
        @SerialName("CBNCode")
        val cbnCode: String = ""

        override fun toString(): String = name
    }

}

@Serializable
class ValidatingCustomerRequest {
    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("BankCode")
    var bankCode: String? = null

    @SerialName("BankCBNCode")
    var bankCbnCode: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = ""
}

@Serializable
class SendCustomerTokenRequest {
    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("DestinationBankCode")
    var destinationBankCode: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = ""

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = ""

    @SerialName("AgentPin")
    var agentPin: String? = ""

    @SerialName("Amount")
    var amount: String? = ""
}


@Serializable
class SendTokenResponse  {
    @SerialName("Status")
    var status: Int? = null

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean = false

    @SerialName("Message")
    var responseMessage: String? = null

    @SerialName("Code")
    var responseCode: String? = null
}

@Serializable
class SubmitTokenRequest{
    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = null

    @SerialName("DestinationBankCode")
    var destinationBankCode: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = ""

    @SerialName("GeoLocation")
    var geoLocation: String? = ""

    @SerialName("DeviceNumber")
    var deviceNumber: Int? = 0

    @SerialName("AgentPin")
    var agentPin: String? = ""

    @SerialName("Token")
    var customerToken: String? = ""

    @SerialName("Amount")
    var amount: String? = ""

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("RequestReference")
    var requestReference: String? = null

    @Serializable
    class Additional {
        @SerialName("TerminalID")
        var terminalId: String? = null

        @SerialName("AgentCode")
        var agentCode: String? = null
    }
}

@Serializable
class SubmitTokenResponse  {
    @SerialName("Status")
    var status: Int? = null

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean = false

    @SerialName("Message")
    var responseMessage: String? = null

    @SerialName("Code")
    var responseCode: String? = null
}