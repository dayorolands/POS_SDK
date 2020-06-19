package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Joseph on 7/18/2018.
 */
@Serializable
class PinChangeRequest {
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("ActivationCode")
    var activationCode: String? = null

    @SerialName("NewPin")
    var newPin: String? = null

    @SerialName("ConfirmNewPin")
    var confirmNewPin: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("OldPin")
    var oldPin: String? = null

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("CustomerToken")
    var customerToken: String? = null

    @SerialName("DeviceID")
    var deviceId: String? = null
}
