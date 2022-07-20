package com.cluster.core.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("AccountName")
    val accountName: String,
    @SerialName("Number")
    val number: String,
    @SerialName("PhoneNumber")
    val phoneNumber: String
)