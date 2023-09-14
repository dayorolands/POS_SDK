package com.cluster.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class NameEnquiryResponse {

    @SerialName("BeneficiaryAccountName")
    var beneficiaryAccountName: String? = null

    @SerialName("NameEnquirySessionID")
    var nameEnquirySessionID: String? = null

    @SerialName("BeneficiaryBVN")
    var beneficiaryBVN: String? = null

    @SerialName("BeneficiaryKYC")
    var beneficiaryKYC: String? = null

    @SerialName("Status")
    var status = false

    @SerialName("ResponseMessage")
    var responseMessage: String? = null
}
