package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("AppVersion")
    val appVersion: String,
    @SerialName("DeviceType")
    val deviceType: Int,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("Password")
    val password: String
)

@Serializable
data class PasswordChangeRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("ConfirmPassword")
    val confirmPassword: String,
    @SerialName("DeviceID")
    val deviceId: String,
    @SerialName("GeoLocation")
    val geoLocation: String,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("OldPassword")
    val oldPassword: String,
    @SerialName("Password")
    val password: String
)

@Serializable
data class TransactionPinChangeRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("ConfirmNewPin")
    val confirmNewPin: String,
    @SerialName("DeviceID")
    val deviceId: String,
    @SerialName("GeoLocation")
    val geoLocation: String,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("NewPin")
    val newPin: String,
    @SerialName("OldPin")
    val oldPin: String
)

@Serializable
data class VerificationRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("DeviceID")
    val deviceId: String,
    @SerialName("InstitutionCode")
    val institutionCode: String = "",
    @SerialName("VerificationCode")
    val verificationCode: String
)

@Serializable
data class ActivationRequest(
    @SerialName("ActivationCode")
    val activationCode: String,
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("ConfirmLoginPassword")
    val confirmLoginPassword: String,
    @SerialName("ConfirmNewTransactionPin")
    val confirmNewTransactionPin: String,
    @SerialName("DeviceID")
    val deviceId: String,
    @SerialName("GeoLocation")
    val geoLocation: String,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("NewLoginPassword")
    val newLoginPassword: String,
    @SerialName("NewTransactionPin")
    val newTransactionPin: String
)