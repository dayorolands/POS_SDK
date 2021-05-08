package com.appzonegroup.app.fasttrack.model.online

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("session_id")
    var sessionId: String? = null,

    @SerializedName("activationCode")
    var activationCode: String? = null
)