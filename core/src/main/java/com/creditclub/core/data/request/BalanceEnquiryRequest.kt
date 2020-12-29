package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */

@Serializable
data class BalanceEnquiryRequest(
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null,

    @SerialName("InstitutionCode")
    var institutionCode: String? = null,

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = null,

    @SerialName("GeoLocation")
    var geoLocation: String? = null,

    @SerialName("AgentPin")
    var agentPin: String? = null,

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null,
)