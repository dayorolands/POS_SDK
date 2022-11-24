package com.cluster.core.data.model

import com.cluster.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Notification(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String?,

    @SerialName("DateLogged")
    @Serializable(with = TimeInstantSerializer::class)
    val dateLogged: Instant?,

    @SerialName("Header")
    val header: String?,

    @SerialName("ID")
    val id: Int,

    @SerialName("IsActive")
    val isActive: Boolean?,

    @SerialName("IsRead")
    val isRead: Boolean?,

    @SerialName("Message")
    val message: String?,

    @SerialName("Reference")
    val reference: String?,

    @SerialName("Type")
    val type: Int?
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

@Serializable
data class NotificationResponse(
    @SerialName("Response")
    val response: List<Notification>? = null,

    @SerialName("Total")
    val total: Int? = 0
)

const val NOTIFICATION_TYPE_1 = 1
const val NOTIFICATION_TYPE_2 = 2