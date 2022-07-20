package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetBankResponse {
    @SerialName("Data")
    val data: List<Data>? = null
    @SerialName("Message")
    val responseMessage: String? = null
    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false
    @SerialName("Code")
    val code: String =""
    @SerialName("Status")
    val status: Int = 0

    @Serializable
    class Data{
        @SerialName("Name")
        val name: String = ""
        @SerialName("ShortName")
        val shortName: String = ""
        @SerialName("Code")
        val dataCode: String = ""
        @SerialName("CBNCode")
        val cbnCode: String = ""

        override fun toString(): String = name
    }

}

@Serializable
class ValidatingCustomerRequest {
    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("BankCode")
    var bankCode: String? = null

    @SerialName("BankCBNCode")
    var bankCbnCode: String? = null

    @SerialName("CustomerAccountNumber")
    var customerAccountNumber: String? = ""
}

