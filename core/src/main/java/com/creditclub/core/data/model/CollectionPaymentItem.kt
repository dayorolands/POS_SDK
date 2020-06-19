package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionPaymentItem {
    @SerialName("ID")
    val id: String? = null

    @SerialName("Name")
    val name: String? = null

    @SerialName("Code")
    val code: String? = null

    @SerialName("Amount")
    val amount: String? = null

    @SerialName("Currency")
    val currency: String? = null

    @SerialName("CollectionAccount")
    val collectionAccount: String? = null
}