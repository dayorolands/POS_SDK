package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CustomerValidationResponse {
    @SerialName("Message")
    var message: String? = null

    @SerialName("Result")
    var result: Result? = null

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean? = false

    @Serializable
    class Result{
        @SerialName("Biller")
        var biller: String? = null

        @SerialName("BillItem")
        var billItem: String? = null

        @SerialName("PaymentReference")
        var paymentReference: String? = null

        @SerialName("AmountDue")
        var amountDue: Double? = null

        @SerialName("CustomerName")
        var customerName: String? = null

        @SerialName("SurCharge")
        var surcharge: Double? = null

        @SerialName("MinimumAmount")
        var minimumAmount: Double? = null

        @SerialName("MaximumAmount")
        var maximumAmount: Double? = null

        @SerialName("AcceptPartPayment")
        var acceptPartPayment: Boolean? = null

        @SerialName("IsSuccessful")
        var isSuccessful: Boolean? = false

        @SerialName("ResponseMessage")
        var responseMessage: String? = null

//        @SerialName("SurchargeConfiguration")
//        var surchargeConfiguration: List<SurchargeConfiguration>? = null

        @SerialName("isMultiplePayment")
        var isMultpilePayment: String? = null
    }
}