package com.creditclub.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
class BillPaymentItem(
    @SerialName("BillerId")
    var billerId: Int? = null,

    @SerialName("ID")
    var id: String? = null,

    @SerialName("Code")
    var paymentCodeField: String? = null,

    @SerialName("Amount")
    var amount: Double? = null,

    var customerFieldOneField: String? = null,

    var customerFieldTwoField: String? = null,

    @SerialName("Name")
    var name: String? = null,
) : Parcelable {
    override fun toString() = name ?: "Payment Item $id"
}