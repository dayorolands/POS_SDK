package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionCustomerValidationRequest(
    @SerialName("Channel")
    val channel: String? = null,

    @SerialName("ItemCode")
    val currency: String? = null,

    @SerialName("Amount")
    val amount: Double? = null,

    @SerialName("CustomFields")
    val customFields: CollectionValidationCustomFields,

    @SerialName("CustomerPhoneNumber")
    val customerPhoneNumber: String? = null,

    @SerialName("CollectionService")
    val collectionService: String? = null,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,

    @SerialName("CustomerEmail")
    val customerEmail: String? = null,

)