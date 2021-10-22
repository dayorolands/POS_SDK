package com.appzonegroup.app.fasttrack.model.online

data class AuthResponse(
    val phoneNumber: String,
    var sessionId: String,
    var activationCode: String = "",
)