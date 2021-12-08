package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Joseph on 6/3/2016.
 */

@Serializable
class BVNRequest {

    @SerialName("ID")
    var id: Long = 0

    @SerialName("BVN")
    var bvn: String? = null

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = null

    @SerialName("CustomerPIN")
    var customerPIN: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("IsSync")
    var isSync: String? = null

    @SerialName("Remark")
    var remark: String? = null

    @SerialName("IsConfirmed")
    var isConfirmed: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null
}
