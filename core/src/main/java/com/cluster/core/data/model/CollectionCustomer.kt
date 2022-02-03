package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionCustomer {
    @SerialName("ID")
    val id: String? = null

    @SerialName("Name")
    val name: String? = null

    @SerialName("PhoneNumber")
    val phoneNumber: String? = null

    @SerialName("Gender")
    val gender: String? = null
}