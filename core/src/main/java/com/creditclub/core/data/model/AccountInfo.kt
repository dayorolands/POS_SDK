package com.creditclub.core.data.model

import com.creditclub.core.util.mask
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/8/2019.
 * Appzone Ltd
 */

@Serializable
data class AccountInfo(
    @SerialName("AccountName")
    val accountName: String = "",

    @SerialName("Number")
    val number: String = "",

    @SerialName("PhoneNumber")
    val phoneNumber: String? = "",

    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false,

    @SerialName("Message")
    private val message: String? = null,
) {

    @SerialName("ReponseMessage")
    val responseMessage: String? = null
        get() = if (field?.isEmpty() == true) message ?: "" else field

    fun displayName() = "$${accountName.mask(4, 2)} - $number"
}
