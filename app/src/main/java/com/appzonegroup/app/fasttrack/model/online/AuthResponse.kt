package com.appzonegroup.app.fasttrack.model.online

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("session_id")
    var sessionId: String,

    @SerializedName("activationCode")
    var activationCode: String = "",
)