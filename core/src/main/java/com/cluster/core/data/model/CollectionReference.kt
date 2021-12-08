package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionReference {
    @SerialName("ReferenceName")
    var referenceName: String? = null

    @SerialName("Reference")
    var reference: String? = null

    @SerialName("CategoryCode")
    var categoryCode: String? = null

    @SerialName("ItemCode")
    var itemCode: String? = null

    @SerialName("CollectionAccount")
    var collectionAccount: String? = null

    @SerialName("CustomerID")
    var customerId: String? = null

    @SerialName("Amount")
    var amount: Double? = null

    @SerialName("Currency")
    var currency: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean? = null

    @SerialName("ResponseMessage")
    var responseMessage: String? = null

    @SerialName("ResponseCode")
    var responseCode: String? = null
}