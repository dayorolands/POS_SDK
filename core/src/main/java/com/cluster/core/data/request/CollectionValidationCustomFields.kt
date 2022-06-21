package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionValidationCustomFields (
    @SerialName("Id")
    var id: String? = null,

    @SerialName("Name")
    var name: String? = null,

    @SerialName("Value")
    var value: String? = null,
)