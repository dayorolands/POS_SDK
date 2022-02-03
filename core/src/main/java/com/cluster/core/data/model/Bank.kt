package com.cluster.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Bank(
    @SerialName("Name")
    val name: String? = null,

    @SerialName("ShortName")
    val shortName: String? = null,

    @SerialName("BankCode")
    val bankCode: String? = null,

    @SerialName("Code")
    val code: String? = null,
) : Parcelable {
    override fun toString() = name ?: "Unnamed bank"
}