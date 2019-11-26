package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoTagCoordinate(
    @SerialName("Longitude") var longitude: String? = null,
    @SerialName("Latitude") var latitude: String? = null
)
