package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/26/2019.
 * Appzone Ltd
 */

@Serializable
class GenericResponse {
    @SerialName("ResponseCode")
    var responseCode = ""

    @SerialName("ResponseMessage")
    var responseMessage = ""
}
