package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PayBillResponse {
    @SerialName("ResponseCode")
    var responseCode: String? = ""

    @SerialName("ResponseMessage")
    var responseMessage: String? = ""

    @SerialName("IsSuccessFul")
    var isSuccessFul: Boolean? = false

    @SerialName("Reference")
    var reference: String? = ""

    @SerialName("RequestStatus")
    var requestStatus: Boolean? = false

    @SerialName("ResponseDescription")
    var responseDescription: String? = ""

    @SerialName("ResponseStatus")
    var responseStatus: String? = ""
}