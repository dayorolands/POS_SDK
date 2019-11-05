package com.appzonegroup.creditclub.pos.models

import com.google.gson.annotations.SerializedName

class NotificationResponse {
    @SerializedName("ResponseCode")
    var responseCode = ""

    @SerializedName("ResponseMessage")
    var message = ""

    @SerializedName("IsSuccessFul")
    var isSuccessFul = false

    @SerializedName("Reference")
    var reference = ""

    @SerializedName("RequestStatus")
    var requestStatus = false

    @SerializedName("ResponseDescription")
    var responseDescription = ""

    @SerializedName("ResponseStatus")
    var responseStatus = ""

    @SerializedName("BillerReference")
    var billerReference: String? = null
}