package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CaseLogResult {
    @SerialName("Status")
    var status: Boolean? = false

    @SerialName("Code")
    var code: String? = ""

    val isSuccessful get() = code == "SUCCESS"

    @SerialName("Message")
    var message: String? = null
}