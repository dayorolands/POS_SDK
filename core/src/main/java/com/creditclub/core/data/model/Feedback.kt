package com.creditclub.core.data.model

import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Feedback(
    @SerialName("ID")
    val id: Int? = null,

    @SerialName("DisplayMessage")
    val displayMessage: Int? = null,

    @SerialName("DateLogged")
    @Serializable(with = TimeInstantSerializer::class)
    val dateLogged: Instant,

    @SerialName("CaseReference")
    val caseReference: String? = null,

    @SerialName("Message")
    val message: String? = null,

    @SerialName("IsAgent")
    val isAgent: Boolean = true,

    @SerialName("IsActive")
    val isActive: Boolean = true,

    @SerialName("Name")
    val name: String,

    @SerialName("LastReadTime")
    val lastReadTime: String? = null,

    @SerialName("FCMToken")
    val fcmToken: String? = null,

    @SerialName("IsAttachment")
    val isAttachment: Boolean = false,

    @SerialName("Blobs")
    val blobs: List<CreditClubImage>? = null,
)
