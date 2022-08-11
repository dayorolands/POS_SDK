package com.cluster.core.data.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgentFee(
    @SerialName("Fee")
    val fee: Double = 0.0,
    @SerialName("TotalFee")
    val totalFee: Double = 0.0,
    @SerialName("Vat")
    val vat: Double = 0.0
)

@Serializable
data class GetFeatureResponse(
    @SerialName("ReponseMessage")
    val responseMessage: String? = null,
    @SerialName("UniqueIdentifier")
    val uniqueIdentifier: String? = null,
    @SerialName("ResponseCode")
    val responseCode: String? = null,
    @SerialName("Data")
    val data : List<FeatureData>,
    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false
)

@Serializable
data class FeatureData(
    @SerialName("Name")
    val name: String? = null,
    @SerialName("Code")
    val code: String? = null,
    @SerialName("UserType")
    val userType: Int = 0,
    @SerialName("IsActive")
    val isActive: Boolean = false,
    @SerialName("DisplayMessage")
    val displayMessage: String? = null,
    @SerialName("ID")
    val id: Int = 0
)
