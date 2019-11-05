package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 09/09/2019.
 * Appzone Ltd
 */
@Serializable
class CaseResponse<T> {
    @SerialName("Response")
    var response: T? = null

    @SerialName("Status")
    var status: Boolean = false

    @SerialName("IsResolved")
    var isResolved: Boolean? = null

    @SerialName("IsClosed")
    var isClosed: Boolean? = null
}