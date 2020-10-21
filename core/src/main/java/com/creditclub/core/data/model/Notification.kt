package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    @SerialName("AgentPhoneNumber")
    val message: String
)

@Serializable
data class NotificationReadResponse(
    @SerialName("Response")
    val response: Boolean
)

@Serializable
data class NotificationRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String?,

    @SerialName("InstitutionCode")
    val institutionCode: String?,

    @SerialName("MaxSize")
    val maxSize: Int?,

    @SerialName("Start")
    val start: Int?
)