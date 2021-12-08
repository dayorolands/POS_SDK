package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 09/09/2019.
 * Appzone Ltd
 */

@Serializable
data class CaseMessageThreadRequest(
    @SerialName("CaseReference")
    val caseReference: String,
)