package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Lga {

    @SerialName("ID")
    var id = ""

    @SerialName("StateID")
    var stateId = ""

    @SerialName("Name")
    var name = ""
}
