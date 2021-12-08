package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CaseCategory {
    @SerialName("ID")
    var id = ""

    @SerialName("Name")
    var name = ""

    override fun toString() = name
}