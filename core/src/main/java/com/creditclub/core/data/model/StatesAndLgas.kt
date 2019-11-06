package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class StatesAndLgas {

    @SerialName("Status")
    var status: Boolean = false

    @SerialName("Message")
    var message: String? = null

    @SerialName("LGAS")
    var lgas: Array<Lga>? = null

    @SerialName("States")
    var states: Array<State>? = null
}
