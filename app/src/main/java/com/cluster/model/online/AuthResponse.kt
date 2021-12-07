package com.cluster.model.online

data class AuthResponse(
    val phoneNumber: String,
    var sessionId: String,
    var activationCode: String = "",
)