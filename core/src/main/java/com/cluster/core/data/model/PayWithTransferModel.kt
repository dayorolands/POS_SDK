package com.cluster.core.data.model

import com.cluster.core.data.response.BackendResponse
import com.cluster.core.data.response.BackendResponseContract
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFee(
    @SerialName("Fee")
    val fee:Double,
    @SerialName("Vat")
    val vat : Double,
    @SerialName("TotalFee")
    val totalFee : Double,
)

@Serializable
data class InitiatePayment(
    @SerialName("TrackingReference")
    val trackingReference: String?,
    @SerialName("VirtualAccountNumber")
    val virtualAccountNumber: String?
)

@Serializable
data class InitiatePaymentRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String?,
    @SerialName("InstitutionCode")
    val institutionCode: String?,
    @SerialName("AgentPin")
    val agentPin: String?,
    @SerialName("CustomerName")
    val customerName: String?,
    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String?,
    @SerialName("Geolocation")
    val geolocation: String?,
    @SerialName("DeviceNumber")
    val deviceNumber: Int?,
    @SerialName("Amount")
    val amount: Int?,
    @SerialName("AdditonalInformation")
    val additionalInformation: String?,
)

@Serializable
data class Additional (
    @SerialName("TerminalID")
    var terminalId: String? = null,
    @SerialName("AgentCode")
    var agentCode: String? = null
)
