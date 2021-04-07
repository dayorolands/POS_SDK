package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateCustomerInfoRequest(
    @SerialName("Amount")
    val amount: Double,
    @SerialName("BillerID")
    val billerId: String,
    @SerialName("CustomerID")
    val customerId: String,
    @SerialName("InstitutionCode")
    val institutionCode: String,
)

@Serializable
data class ValidateCustomerInfoResponse(
    @SerialName("AdditionalInformation")
    val additionalInformation: String = "",
    @SerialName("CustomerAccountNumber")
    val customerAccountNumber: String = "",
    @SerialName("CustomerAddress")
    val customerAddress: String = "",
    @SerialName("CustomerEmail")
    val customerEmail: String = "",
    @SerialName("CustomerID")
    val customerID: String = "",
    @SerialName("CustomerName")
    val customerName: String = "",
    @SerialName("CustomerPhone")
    val customerPhone: String = "",
    @SerialName("CustomerType")
    val customerType: String = "",
    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false,
    @SerialName("ResponseCode")
    val responseCode: String = "",
    @SerialName("ResponseMessage")
    val responseMessage: String = "",
    @SerialName("ValidationCode")
    val validationCode: String = "",
)