package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FundsTransferCustomerRequest {

    @SerialName("ExternalTransactionReference")
    var externalTransactionReference = ""

    @SerialName("BeneficiaryAccountNumber")
    var beneficiaryAccountNumber = ""

    @SerialName("NameEnquirySessionID")
    var nameEnquirySessionID: String? = null

    @SerialName("BeneficiaryInstitutionCode")
    var beneficiaryInstitutionCode = ""

    @SerialName("AuthToken")
    var authToken = ""

    @SerialName("AmountInNaira")
    var amountInNaira = 0.0

    @SerialName("IsToRelatedCommercialBank")
    var isToRelatedCommercialBank = true // for transfer to same institution this is set to true

    @SerialName("Narration")
    var narration = ""
}
