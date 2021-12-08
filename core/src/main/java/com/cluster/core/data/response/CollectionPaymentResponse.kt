package com.cluster.core.data.response

import com.cluster.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
class CollectionPaymentResponse {
    @SerialName("ReceiptDetails")
    var receiptDetails: String? = null

    @SerialName("ReceiptReference")
    var receiptReference: String? = null

    @SerialName("Amount")
    var amount: Double? = null

    @SerialName("CollectionPaymentItemName")
    var collectionPaymentItemName: String? = null

    @SerialName("CollectionCategoryName")
    var collectionCategoryName: String? = null

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

    @SerialName("Date")
    @Serializable(with = TimeInstantSerializer::class)
    var date: Instant? = null
}