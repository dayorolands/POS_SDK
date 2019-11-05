package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */

@Serializable
class BalanceEnquiryRequest {
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = ""

    @SerialName("InstitutionCode")
    var institutionCode: String? = ""

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = ""

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = ""

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null
}