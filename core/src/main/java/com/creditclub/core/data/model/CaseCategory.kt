package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CaseCategory {
    @SerialName("ID")
    var id = ""

    @SerialName("Name")
    var name = ""
}