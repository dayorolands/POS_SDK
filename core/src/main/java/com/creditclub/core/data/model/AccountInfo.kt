package com.creditclub.core.data.model

import com.creditclub.core.util.mask
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/8/2019.
 * Appzone Ltd
 */

@Serializable
class AccountInfo {
    @SerialName("AccountName")
    var accountName: String = ""

    @SerialName("Number")
    var number: String = ""

    @SerialName("PhoneNumber")
    var phoneNumber: String? = ""

    @SerialName("ReponseMessage")
    var responseMessage: String? = ""
        get() = if (field?.isEmpty() == true) message ?: "" else field

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean = false

    @SerialName("Message")
    private val message: String? = null

    fun displayName() = "$${accountName.mask(4, 2)} - $number"
}
