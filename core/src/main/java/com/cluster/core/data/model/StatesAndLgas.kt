package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class StatesAndLgas {

    @SerialName("Status")
    var status: Boolean = false

    @SerialName("Message")
    var message: String? = null

    @SerialName("Data")
    var data: List<State>? = null
}
