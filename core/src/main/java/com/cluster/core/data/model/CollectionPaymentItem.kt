package com.cluster.core.data.model

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

    @SerialName("IsFixedAmount")
    var isFixedAmount: Boolean? = false

    @SerialName("CustomFields")
    var customFields: List<CollectionValidationCustomFields>? = null

    @Serializable
    class CollectionValidationCustomFields {
        @SerialName("Id")
        var customId: Int? = null

        @SerialName("Name")
        var customName: String? = null

        @SerialName("DisplayText")
        var displayText: String? = null

        @SerialName("IsRequired")
        var isRequired: Boolean? = false
    }

    override fun toString() = "$name"
}