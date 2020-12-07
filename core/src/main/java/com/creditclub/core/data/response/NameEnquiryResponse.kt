package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/26/2019.
 * Appzone Ltd
 */

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
