package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PayBillRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String?,

    @SerialName("AgentCode")
    val agentCode: String? = null,

    @SerialName("AgentPin")
    val agentPin: String?,

    @SerialName("BillerID")
    val merchantBillerIdField: String?,

    @SerialName("BillerName")
    val billerName: String?,

    @SerialName("BillerCategoryID")
    val billerCategoryID: String?,

    @SerialName("BillerCategoryName")
    val billerCategoryName: String?,

    @SerialName("PaymentItemID")
    val billItemID: String?,

    @SerialName("PaymentItemCode")
    val paymentItemCode: String?,

    @SerialName("PaymentItemName")
    val paymentItemName: String?,

    @SerialName("CustomerID")
    val customerId: String?,

    @SerialName("CustomerDepositSlipNumber")
    val customerDepositSlipNumber: String?,

    @SerialName("CustomerName")
    val customerName: String?,

    @SerialName("AccountNumber")
    val accountNumber: String?,

    @SerialName("CustomerEmail")
    val customerEmail: String?,

    @SerialName("CustomerPhone")
    val customerPhone: String?,

    @SerialName("Amount")
    val amount: String,

    @SerialName("InstitutionCode")
    val institutionCode: String?,

    @SerialName("Geolocation")
    val geolocation: String?,

    @SerialName("IsRecharge")
    val isRecharge: Boolean,

    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String,

    @SerialName("DeviceNumber")
    val deviceNumber: Int? = null,

    @SerialName("ValidationCode")
    val validationCode: String?,
)
