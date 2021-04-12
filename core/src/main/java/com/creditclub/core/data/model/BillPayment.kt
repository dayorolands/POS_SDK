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
    val additionalInformation: String? = null,
    @SerialName("CustomerAccountNumber")
    val customerAccountNumber: String? = null,
    @SerialName("CustomerAddress")
    val customerAddress: String? = null,
    @SerialName("CustomerEmail")
    val customerEmail: String? = null,
    @SerialName("CustomerID")
    val customerID: String? = null,
    @SerialName("CustomerName")
    val customerName: String? = null,
    @SerialName("CustomerPhone")
    val customerPhone: String? = null,
    @SerialName("CustomerType")
    val customerType: String? = null,
    @SerialName("IsSuccessful")
    val isSuccessful: Boolean = false,
    @SerialName("ResponseCode")
    val responseCode: String? = null,
    @SerialName("ResponseMessage")
    val responseMessage: String? = null,
    @SerialName("ValidationCode")
    val validationCode: String? = null,
)