package com.creditclub.core.data.request

import com.creditclub.core.data.model.GeoTagCoordinate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/11/2019.
 * Appzone Ltd
 */

@Serializable
class OfflineHLATaggingRequest {

    @SerialName("Name")
    var name: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("Pictures")
    var pictures: Array<String?>? = null

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