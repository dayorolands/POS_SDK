package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PayBillResponse(
    @SerialName("ResponseCode")
    val responseCode: String? = "",
    @SerialName("ResponseMessage")
    val responseMessage: String? = "",
    @SerialName("IsSuccessFul")
    val isSuccessFul: Boolean? = false,
    @SerialName("Reference")
    val reference: String? = "",
    @SerialName("AdditionalInformation")
    val additionalInformation: String? = "",
) {
    @Serializable
    data class AdditionalInformation(
        @SerialName("CustomerAddress")
        val customerAddress: String? = null,
        @SerialName("CustomerToken")
        val customerToken: String? = null,
    )
}