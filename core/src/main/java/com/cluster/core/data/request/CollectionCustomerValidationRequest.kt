package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionCustomerValidationRequest {
    @SerialName("Channel")
    var channel: String? = null

    @SerialName("ItemCode")
    var itemCode: String? = null

    @SerialName("Amount")
    var amount: Double? = null

    @SerialName("CustomFields")
    var customFields: String? = null

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = null

    @SerialName("CustomerName")
    var customerName: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("CustomerEmail")
    var customerEmail: String? = null

    @Serializable
    class CustomFields {
        @SerialName("Id")
        var id: Int? = null

        @SerialName("Name")
        var name: String? = null

        @SerialName("Value")
        var value: String? = null
    }

}