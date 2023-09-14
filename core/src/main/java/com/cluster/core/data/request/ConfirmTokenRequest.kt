package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class ConfirmTokenRequest {
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = null

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("Token")
    var token: String = ""

    @SerialName("ReferenceNumber")
    var referenceNumber: String = ""
}