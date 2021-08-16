package com.creditclub.core.data.request

import com.creditclub.core.util.generateRRN
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
) {
    @Serializable
    data class Additional(
        @SerialName("Currency")
        val currency: String = "NGN",
    )
}
