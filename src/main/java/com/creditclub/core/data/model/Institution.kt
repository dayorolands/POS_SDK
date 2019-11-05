package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Institution {

    @SerialName("Name")
    var name: String = ""

    @SerialName("InstitutionCode")
    var institutionCode: String = ""
}
