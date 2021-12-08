package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class Balance {
    @SerialName("ResponseMessage")
    var responseMessage: String? = null

    @SerialName("AvailableBalance")
    var availableBalance: Double = 0.0

    @SerialName("Balance")
    var balance: Double = 0.0

    @SerialName("IsSussessful")
    var isSussessful: Boolean = false
}
