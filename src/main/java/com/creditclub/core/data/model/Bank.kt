package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Bank {

    @SerialName("Name")
    var name: String? = null

    @SerialName("ShortName")
    var shortName: String? = null

    @SerialName("BankCode")
    var bankCode: String? = null

    @SerialName("Code")
    var code: String? = null
}