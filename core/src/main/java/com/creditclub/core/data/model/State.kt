package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class State {

    @SerialName("ID")
    var id = ""

    @SerialName("Name")
    var name = ""
}
