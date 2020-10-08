package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CollectionCategory {
    @SerialName("ID")
    val id: String? = null

    @SerialName("Name")
    val name: String? = null

    @SerialName("Code")
    val code: String? = null

    override fun toString() = "$name"
}