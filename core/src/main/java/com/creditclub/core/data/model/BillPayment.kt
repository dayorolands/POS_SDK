package com.creditclub.core.data.model

import android.os.Parcelable
import com.creditclub.core.data.response.BackendResponse
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateCustomerInfoRequest(
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
    val deviceNumber: Int,

    @SerialName("ValidationCode")
    val validationCode: String?,

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,
)

@Serializable
class PayBillResponse(
    @SerialName("ResponseMessage")
    override val responseMessage: String? = "",

    @SerialName("IsSuccessFul")
    val isSuccessFul: Boolean? = false,

    @SerialName("Reference")
    val reference: String? = "",

    @SerialName("AdditionalInformation")
    val additionalInformation: String? = null,
) : BackendResponse() {

    @Serializable
    data class AdditionalInformation(
        @SerialName("CustomerAddress")
        val customerAddress: String? = null,

        @SerialName("CustomerToken")
        val customerToken: String? = null,
    )
}

@Serializable
data class BillCategory(
    @SerialName("ID")
    var id: String,

    @SerialName("Name")
    var name: String,

    @SerialName("Description")
    var description: String? = null,

    @SerialName("IsAirtime")
    var isAirtime: Boolean = false,
) {
    override fun toString() = name
}

@Serializable
@Parcelize
data class Biller(
    @SerialName("BillerCategoryID")
    var billerCategoryId: Int? = null,

    @SerialName("CategoryId")
    var categoryId: String? = null,

    @SerialName("ID")
    var id: String? = null,

    @SerialName("Name")
    var name: String,

    @SerialName("CustomerField1")
    var customerField1: String? = null,

    @SerialName("CustomerField2")
    var customerField2: String? = null,
) : Parcelable {
    override fun toString() = name
}

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

    @SerialName("Name")
    var name: String? = null,
) : Parcelable {
    override fun toString() = name ?: "Payment Item $id"
}