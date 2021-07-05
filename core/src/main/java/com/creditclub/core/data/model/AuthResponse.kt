package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("session_id")
    val sessionId: String? = null,
    @SerialName("activationCode")
    val activationCode: String? = null,
)
