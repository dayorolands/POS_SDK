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

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = ""

    @Serializable
    class AdditionalInformation {
        @SerialName("CustomerAddress")
        var customerAddress: String? = null

        @SerialName("CustomerToken")
        var customerToken: String? = null
    }
}