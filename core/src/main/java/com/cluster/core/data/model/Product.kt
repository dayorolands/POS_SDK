package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("ID")
    val id: Long = 0,

    @SerialName("Name")
    val name: String = "",

    @SerialName("Code")
    val code: String = "",

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,
)
