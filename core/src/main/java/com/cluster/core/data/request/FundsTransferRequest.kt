package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FundsTransferRequest(
    @SerialName("ExternalTransactionReference")
    val externalTransactionReference: String? = null,

    @SerialName("BeneficiaryAccountNumber")
    val beneficiaryAccountNumber: String? = null,

    @SerialName("BeneficiaryAccountName")
    val beneficiaryAccountName: String? = null,

    @SerialName("BeneficiaryBVN")
    val beneficiaryBVN: String? = null,

    @SerialName("BeneficiaryKYC")
    val beneficiaryKYC: String? = null,

    @SerialName("NameEnquirySessionID")
    val nameEnquirySessionID: String? = null,

    @SerialName("BeneficiaryInstitutionCode")
    val beneficiaryInstitutionCode: String? = null,

    @SerialName("AuthToken")
    val authToken: String? = null,

    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("AgentCode")
    val agentCode: String? = null,

    @SerialName("AgentPin")
    val agentPin: String? = null,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,

    @SerialName("GeoLocation")
    val geoLocation: String? = null,

    @SerialName("AmountInNaira")
    val amountInNaira: Double = 0.0,

    @SerialName("IsNotEncryptedPin")
    val isNotEncryptedPin: Boolean = true,

    @SerialName("IsToRelatedCommercialBank")
    val isToRelatedCommercialBank: Boolean = true, // for transfer to same institution this is set to true

    @SerialName("Narration")
    val narration: String? = null,

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,

    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String?,

    @SerialName("DeviceNumber")
    val deviceNumber: Int = 0,
) {
    @Serializable
    data class Additional(
        @SerialName("Currency")
        val currency: String = "NGN",
    )
}

@Serializable
data class POSCashoutRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("AgentCode")
    val agentCode: String? = null,

    @SerialName("Amount")
    val amount: Double = 0.0,

    @SerialName("TransactionReference")
    val transactionReference: String?,

    @SerialName("DeviceNumber")
    val deviceNumber: Int = 0,

    @SerialName("PAN")
    var maskedPan: String? = "",

    @SerialName("STAN")
    var transactionStan: String? = "",

    @SerialName("CardType")
    var cardType: String? = "",

    @SerialName("ExpiryDate")
    var expiryDate: String? = "",

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = "",

    @SerialName("CardHolder")
    var cardHolderName: String? = "",
)

@Serializable
data class CollectionReportRequest(
    @SerialName("DeviceNumber")
    val deviceNumber: Int = 0,

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = "",

    @SerialName("CustomerName")
    var customerName: String? = "",
)

@Serializable
data class CrossBankRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("Amount")
    val amount: String? = "",

    @SerialName("CustomerAccountNumber")
    val customerAccountNumber: String?,

    @SerialName("DeviceNumber")
    val deviceNumber: Int = 0,

    @SerialName("InstitutionCode")
    val institutionCode: String? = "",

    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String? = "",
)


@Serializable
data class PWTReceiptRequest(
    @SerialName("VirtualAccount")
    val virtualAccountNumber: String? = "",
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = "",
    @SerialName("AgentCode")
    val agentCode : String? = "",
    @SerialName("Narration")
    val narration : String? = "",
    @SerialName("CustomerAccountName")
    val customerAccountName : String = "",
    @SerialName("CustomerAccountNumber")
    val customerAcctNumber : String? = "",
    @SerialName("AmountReceived")
    val amountReceived: Double = 0.00,
    @SerialName("CustomerName")
    val customerName: String? = "",
    @SerialName("AgentAccountName")
    val agentAccountName: String = "",
    @SerialName("Date")
    val date: String? = "",
    @SerialName("RRN")
    val rrn: String? = "",
    @SerialName("ExpectedAmount")
    val expectedAmount: Double = 0.00,
    @SerialName("AgentAccount")
    val agentAccount : String = ""
)