package com.cluster.core.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("AccountName")
    val accountName: String? = null,
    @SerialName("Number")
    val number: String? = null,
    @SerialName("PhoneNumber")
    val phoneNumber: String? = null
)