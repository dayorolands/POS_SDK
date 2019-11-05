package com.creditclub.core.data.model

import com.creditclub.core.data.response.BackendResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/14/2019.
 * Appzone Ltd
 */

@Serializable
class CustomerAccount {
    @SerialName("Name")
    var name: String? = null

    @SerialName("PhoneNumber")
    var phoneNumber: String? = null

    @SerialName("LinkingBankAccounts")
    var linkingBankAccounts: ArrayList<AccountInfo>? = null

    @SerialName("ReponseMessage")
    var responseMessage: String? = null
        get() = if (field?.isEmpty() == true) message ?: "" else field

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean = false

    @SerialName("Message")
    private val message: String? = null

    fun getAccountNames(): ArrayList<String>? {
        return linkingBankAccounts?.run {
            val names = ArrayList<String>()
            for (accountInfo in this) {
                names.add("${accountInfo.accountName} - ${accountInfo.number}")
            }

            return names
        }
    }
}
