package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 09/09/2019.
 * Appzone Ltd
 */

@Serializable
class CaseDetailsRequest {
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("StartIndex")
    val startIndex = 0

    @SerialName("MaxSize")
    var maxSize = 10
}