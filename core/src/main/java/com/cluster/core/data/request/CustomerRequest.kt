package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CustomerRequest {
    @SerialName("CustomerLastName")
    var customerLastName: String? = ""

    @SerialName("CustomerFirstName")
    var customerFirstName: String? = ""

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = ""

    @SerialName("Gender")
    var gender: String? = ""

    @SerialName("Address")
    var address: String? = null

    @SerialName("DateOfBirth")
    var dateOfBirth: String? = null

    @SerialName("PlaceOfBirth")
    var placeOfBirth: String? = null

    @SerialName("NOKPhone")
    var nokPhone: String? = null

    @SerialName("NOKName")
    var nokName: String? = null

    @SerialName("StarterPackNumber")
    var starterPackNumber: String? = null

    @SerialName("ProductCode")
    var productCode: String? = null

    @SerialName("ProductName")
    var productName: String? = null

    @SerialName("AccountNumber")
    var accountNumber: String? = null

    @SerialName("PIN")
    var pin: String? = null

    @SerialName("BVN")
    var bvn: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("UniqueReferenceID")
    var uniqueReferenceID: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = null

    @SerialName("DeviceNumber")
    var deviceNumber: Int? = null

    @Serializable
    class Additional {
        @SerialName("MiddleName")
        var middleName: String? = null

        @SerialName("Province")
        var province: String? = null

        @SerialName("Occupation")
        var occupation: String? = null

        @SerialName("Passport")
        var passport: String? = null

        @SerialName("Signature")
        var signature: String? = null

        @SerialName("IdCard")
        var idCard: String? = null

        @SerialName("Email")
        var email: String? = null

        @SerialName("Currency")
        var currency: String? = null

        @SerialName("Title")
        var title: String? = null

        @SerialName("Country")
        var country: String? = null

        @SerialName("State")
        var state: String? = null

        @SerialName("City")
        var lga: String? = null
    }
}
