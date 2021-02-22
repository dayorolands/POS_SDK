package com.creditclub.core.data.request

import com.creditclub.core.util.generateRRN
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PayBillRequest(
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null,

    @SerialName("AgentPin")
    var agentPin: String? = null,

    @SerialName("BillerID")
    var merchantBillerIdField: String? = null,

    @SerialName("BillerName")
    var billerName: String? = null,

    @SerialName("BillerCategoryID")
    var billerCategoryID: String? = null,

    @SerialName("BillerCategoryName")
    var billerCategoryName: String? = null,

    @SerialName("PaymentItemID")
    var billItemID: String? = null,

    @SerialName("PaymentItemCode")
    var paymentItemCode: String? = null,

    @SerialName("PaymentItemName")
    var paymentItemName: String? = null,

    @SerialName("CustomerID")
    var customerId: String? = null,

    @SerialName("CustomerDepositSlipNumber")
    var customerDepositSlipNumber: String? = null,

    @SerialName("CustomerName")
    var customerName: String? = null,

    @SerialName("AccountNumber")
    var accountNumber: String? = null,

    @SerialName("CustomerEmail")
    var customerEmail: String? = null,

    @SerialName("CustomerPhone")
    var customerPhone: String? = null,

    @SerialName("Amount")
    var amount: String? = null,

    @SerialName("InstitutionCode")
    var institutionCode: String? = null,

    @SerialName("Geolocation")
    var geolocation: String? = null,

    @SerialName("IsRecharge")
    var isRecharge: Boolean = false,

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = generateRRN(),
)
