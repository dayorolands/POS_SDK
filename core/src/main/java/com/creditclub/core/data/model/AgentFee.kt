package com.creditclub.core.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgentFee(
    @SerialName("Fee")
    val fee: Double = 0.0,
    @SerialName("TotalFee")
    val totalFee: Double = 0.0,
    @SerialName("Vat")
    val vat: Double = 0.0
)