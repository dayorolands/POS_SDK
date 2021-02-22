package com.creditclub.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Biller(
    @SerialName("Description")
    var description: String? = null,

    @SerialName("IsAirtime")
    var isAirtime: Boolean? = null,

    @SerialName("BillerCategoryID")
    var billerCategoryId: Int? = null,

    @SerialName("CategoryId")
    var categoryId: String? = null,

    @SerialName("ID")
    var id: String? = null,

    @SerialName("Name")
    var name: String? = null,

    @SerialName("Status")
    var status: Boolean? = null,

    @SerialName("Amount")
    var amount: String? = null,

    @SerialName("CustomerField1")
    var customerField1: String? = null,

    @SerialName("CustomerField2")
    var customerField2: String? = null,
) : Parcelable {
    override fun toString() = name ?: "Biller $id"
}
