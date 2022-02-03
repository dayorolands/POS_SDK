package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 09/09/2019.
 * Appzone Ltd
 */

@Serializable
data class CaseDetailsRequest(
    @SerialName("AgentPhoneNumber") val agentPhoneNumber: String? = null,
    @SerialName("InstitutionCode") val institutionCode: String? = null,
    @SerialName("StartIndex") val startIndex: Int = 0,
    @SerialName("MaxSize") val maxSize: Int = 10
)