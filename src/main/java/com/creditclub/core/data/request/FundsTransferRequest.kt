package com.creditclub.core.data.request

import com.creditclub.core.util.generateRRN
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FundsTransferRequest {

    @SerialName("ExternalTransactionReference")
    var externalTransactionReference: String? = null

    @SerialName("BeneficiaryAccountNumber")
    var beneficiaryAccountNumber: String? = null

    @SerialName("BeneficiaryAccountName")
    var beneficiaryAccountName: String? = null

    @SerialName("BeneficiaryBVN")
    var beneficiaryBVN: String? = null

    @SerialName("BeneficiaryKYC")
    var beneficiaryKYC: String? = null

    @SerialName("NameEnquirySessionID")
    var nameEnquirySessionID: String? = null

    @SerialName("BeneficiaryInstitutionCode")
    var beneficiaryInstitutionCode: String? = null

    @SerialName("AuthToken")
    var authToken: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("AmountInNaira")
    var amountInNaira = 0.0

    @SerialName("IsNotEncryptedPin")
    var isNotEncryptedPin = true

    @SerialName("IsToRelatedCommercialBank")
    var isToRelatedCommercialBank = true // for transfer to same institution this is set to true

    @SerialName("Narration")
    var narration: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = generateRRN()

    @Serializable
    class Additional {

        @SerialName("Currency")
        var currency = ""
    }
}
