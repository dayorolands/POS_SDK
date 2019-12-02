package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */

@Serializable
open class ApiResponse<T> {
    @SerialName("Message")
    var message: String = ""

    @SerialName("Status")
    var status: String = "06"

    @SerialName("Data")
    var data: T? = null

    fun isSuccessful() = status == "00"
}