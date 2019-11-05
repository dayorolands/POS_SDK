package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 09/09/2019.
 * Appzone Ltd
 */

@Serializable
class CaseMessageThreadRequest {
    @SerialName("CaseReference")
    var caseReference = ""
}