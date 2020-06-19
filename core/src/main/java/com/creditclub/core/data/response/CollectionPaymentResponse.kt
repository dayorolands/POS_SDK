package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionPaymentResponse {
    @SerialName("ReceiptDetails")
    var receiptDetails: String? = null

    @SerialName("ReceiptReference")
    var receiptReference: String? = null

    @SerialName("CollectionReference")
    var collectionReference: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean? = null

    @SerialName("ResponseMessage")
    var responseMessage: String? = null

    @SerialName("ResponseCode")
    var responseCode: String? = null
}