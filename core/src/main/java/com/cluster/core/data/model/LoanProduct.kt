package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class LoanProduct(
    @SerialName("ID")
    val id: Long = 0,

    @SerialName("Name")
    val name: String? = null,

    @SerialName("MinimumAmount")
    val minimumAmount: Double = 0.0,

    @SerialName("MaximumAmount")
    val maximumAmount: Double = 0.0,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,
) {
    override fun toString() = "$name (N$minimumAmount - N$maximumAmount)"
}

@Serializable
data class AgentLoanRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String?,
    @SerialName("AgentPin")
    val agentPin: String,
    @SerialName("Amount")
    val amount: Double,
    @SerialName("DeviceNumber")
    val deviceNumber: Int,
    @SerialName("FeeAmount")
    val feeAmount: Double,
    @SerialName("GeoLocation")
    val geoLocation: String,
    @SerialName("InstitutionCode")
    val institutionCode: String?,
    @SerialName("LoanProductID")
    val loanProductId: Int,
    @SerialName("RequestReference")
    val requestReference: String,
    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String,
    @SerialName("Tenure")
    val tenure: Int,
)

@Serializable
data class AgentLoan(
    @SerialName("LoanProductID")
    val loanProductId: Int = 1,
    @SerialName("FeeRate")
    val feeRate: Double = 0.0,
    @SerialName("IsEligible")
    val isEligible: Boolean = false,
    @SerialName("LastRequestDate")
    val lastRequestDate: Boolean = false,
    @SerialName("MaxAmount")
    val maxAmount: Double = 0.0,
    @SerialName("Message")
    val message: String,
    @SerialName("ProductName")
    val productName: String? = null,
    @SerialName("Tenure")
    val tenure: Int = 0
)
