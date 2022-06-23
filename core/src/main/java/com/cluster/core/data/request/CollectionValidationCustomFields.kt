package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionValidationCustomFields {
    @SerialName("Id")
    var id: Int? = null

    @SerialName("Name")
    var name: String? = null

    @SerialName("Value")
    var value: String? = null
}