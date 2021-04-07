package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillCategory(
    @SerialName("ID")
    var id: String? = null,

    @SerialName("Name")
    var name: String? = null,

    @SerialName("Description")
    var description: String? = null,

    @SerialName("IsAirtime")
    var isAirtime: Boolean = false,
) {
    override fun toString() = name ?: "Category $id"
}

