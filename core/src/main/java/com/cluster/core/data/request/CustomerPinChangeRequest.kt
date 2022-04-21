package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Joseph on 7/18/2018.
 */
@Serializable
data class CustomerPinChangeRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,

    @SerialName("ActivationCode")
    val activationCode: String? = null,

    @SerialName("NewPin")
    val newPin: String? = null,

    @SerialName("ConfirmNewPin")
    val confirmNewPin: String? = null,

    @SerialName("GeoLocation")
    val geoLocation: String? = null,

    @SerialName("OldPin")
    val oldPin: String? = null,

    @SerialName("CustomerPhoneNumber")
    val customerPhoneNumber: String? = null,

    @SerialName("AgentPin")
    val agentPin: String? = null,

    @SerialName("CustomerToken")
    val customerToken: String? = null,

    @SerialName("DeviceID")
    val deviceId: String? = null,
)
