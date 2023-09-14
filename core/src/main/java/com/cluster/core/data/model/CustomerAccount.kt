package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CustomerAccount(
    @SerialName("Name")
    val name: String? = null,

    @SerialName("PhoneNumber")
    val phoneNumber: String? = null,

    @SerialName("LinkingBankAccounts")
    val linkingBankAccounts: List<AccountInfo>? = null,

    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false,

    @SerialName("Message")
    private val message: String? = null,
) {
    @SerialName("ReponseMessage")
    val responseMessage: String? = null
        get() = if (field?.isEmpty() == true) message ?: "" else field
}