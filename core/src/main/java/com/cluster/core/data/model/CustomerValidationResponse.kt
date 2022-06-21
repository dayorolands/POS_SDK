package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CustomerValidationResponse {
    @SerialName("Biller")
    var biller: String? = null

    @SerialName("BillItem")
    var billItem: String? = null

    @SerialName("PaymentReference")
    var paymentReference: String? = null

    @SerialName("AmountDue")
    var amountDue: String? = null

    @SerialName("CustomerName")
    var customerName: String? = null

    @SerialName("Surcharge")
    var surcharge: Double? = null

    @SerialName("MinimumAmount")
    var minimumAmount: Double? = null

    @SerialName("MaximumAmount")
    var maximumAmount: Double? = null

    @SerialName("AcceptPartPayment")
    var acceptPartPayment: Boolean? = null

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean? = null

    @SerialName("ResponseMessage")
    var responseMessage: String? = null

    @SerialName("isMultiplePayment")
    var isMultpilePayment: String? = null
}