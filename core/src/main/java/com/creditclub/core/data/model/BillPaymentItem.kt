package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BillPaymentItem {
    @SerialName("BillerId")
    var billerId: Int? = null

    @SerialName("ID")
    var id: String? = null

    @SerialName("Code")
    var paymentCodeField: String? = null
    var principalAccountNumberField: String? = null
    var surchargeAccountNumberField: String? = null
    var narrationPrefixField: String? = null
    var isCanModifyPriceField = false

    @SerialName("Amount")
    var amount: Double? = null

    var customerFieldOneField: String? = null

    var customerFieldTwoField: String? = null

    @SerialName("Name")
    var name: String? = null

    var propertyChanged: String? = null
}