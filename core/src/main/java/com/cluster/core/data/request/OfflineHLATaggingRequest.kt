package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class OfflineHLATaggingRequest {
    @SerialName("Name")
    var name: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("Pictures")
    var pictures: MutableList<String?>? = null

    @SerialName("Description")
    var description: String? = null

    @SerialName("State")
    var state: String? = null

    @SerialName("LGA")
    var lga: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("Location")
    var location: GeoTagCoordinate? = null

    @SerialName("DateTagged")
    var dateTagged: String? = null
}

@Serializable
data class GeoTagCoordinate(
    @SerialName("Latitude") val latitude: String,
    @SerialName("Longitude") val longitude: String,
)
