package com.cluster.core.data.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoralPayReferenceRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("AgentPin")
    val agentPin: String,
    @SerialName("Amount")
    val amount: Int = 0,
    @SerialName("GeoLocation")
    val geoLocation: String?,
    @SerialName("InstitutionCode")
    val institutionCode: String,
)