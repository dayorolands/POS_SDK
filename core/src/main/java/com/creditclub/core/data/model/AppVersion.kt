package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersion(

    @SerialName("Link")
    var link: String? = null,

    @SerialName("Version")
    var version: String? = null
)