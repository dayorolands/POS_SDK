package com.cluster.core.data.model

import com.cluster.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

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
data class AgentLoanEligibility(
    @SerialName("LoanProductID")
    val loanProductId: Int,
    @SerialName("FeeRate")
    val feeRate: Double = 0.0,
    @SerialName("IsEligible")
    val isEligible: Boolean = false,
    @SerialName("LastRequestDate")
    val lastRequestDate: Boolean = false,
    @SerialName("MaxAmount")
    val maxAmount: Double = 0.0,
    @SerialName("Message")
    val message: String? = null,
    @SerialName("ProductName")
    val productName: String? = null,
    @SerialName("Tenure")
    val tenure: Int = 0
)

@Serializable
data class AgentLoanRecord(
    @SerialName("DisbursementDate")
    @Serializable(with = TimeInstantSerializer::class)
    val disbursementDate: Instant? = null,
    @SerialName("Fee")
    val fee: Double = 0.0,
    @SerialName("Name")
    val name: String = "",
    @SerialName("QualifiedAmount")
    val qualifiedAmount: Double = 0.0,
    @SerialName("RepaymentDate")
    @Serializable(with = TimeInstantSerializer::class)
    val repaymentDate: Instant? = null,
    @SerialName("RequestedAmount")
    val requestedAmount: Double = 0.0,
    @SerialName("TerminalID")
    val terminalId: String = ""
)

@Serializable
data class AgentLoanSearchRequest(
    @SerialName("FromDate")
    val fromDate: String = "",
    @SerialName("InstitutionCode")
    val institutionCode: String = "",
    @SerialName("MaxSize")
    val maxSize: Int = 0,
    @SerialName("PhoneNumber")
    val phoneNumber: String = "",
    @SerialName("StartIndex")
    val startIndex: Int = 0,
    @SerialName("ToDate")
    val toDate: String = ""
)