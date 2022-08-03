package com.cluster.core.data.model


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

@Serializable
data class GetFeatureResponse(
    @SerialName("Name")
    val name: String? = null,
    @SerialName("Code")
    val code: String? = null,
    @SerialName("IsActive")
    val isActive: Boolean = false,
    @SerialName("DisplayMessage")
    val displayMessage: String? = null,
    @SerialName("ID")
    val id: Int = 0,
)