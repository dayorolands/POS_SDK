package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionPaymentItem {
    @SerialName("ID")
    var id: String? = null

    @SerialName("Name")
    var name: String? = null

    @SerialName("Code")
    var code: String? = null

    @SerialName("Amount")
    var amount: String? = null

    @SerialName("Currency")
    var currency: String? = null

    @SerialName("CollectionAccount")
    var collectionAccount: String? = null

    override fun toString() = "$name - $code"
}