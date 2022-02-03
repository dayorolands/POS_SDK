package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Lga {

    @SerialName("ID")
    var id = ""

    @SerialName("Name")
    var name = ""

    @SerialName("State")
    var state: State? = null

    override fun toString() = name
}
