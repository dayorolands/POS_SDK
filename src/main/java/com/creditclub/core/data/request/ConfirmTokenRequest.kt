package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */
@Serializable
class ConfirmTokenRequest {
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = null

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("Token")
    var token: String = ""

    @SerialName("ReferenceNumber")
    var referenceNumber: String = ""
}