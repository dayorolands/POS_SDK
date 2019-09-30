package com.appzonegroup.app.fasttrack.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/14/2019.
 * Appzone Ltd
 */
@Serializable
class Customer {
    @SerialName("ProductCode")
    var productCode: String? = null

    @SerialName("ProductName")
    var productName: String? = null

    @SerialName("AccountNumber")
    var accountNumber: String? = null

    @SerialName("PIN")
    var PIN: String? = null

    @SerialName("CustomerLastName")
    var customerLastName: String? = null

    @SerialName("CustomerFirstName")
    var customerFirstName: String? = null

    @SerialName("CustomerPhoneNumber")
    var customerPhoneNumber: String? = null

    @SerialName("Gender")
    var gender: String? = null

    @SerialName("StarterPackNumber")
    var starterPackNumber: String? = null

    @SerialName("BVN")
    var BVN: String? = null

    @SerialName("Address")
    var address: String? = null

    @SerialName("NOKPhone")
    var NOKPhone: String? = null

    @SerialName("NOKName")
    var NOKName: String? = null

    @SerialName("PlaceOfBirth")
    var placeOfBirth: String? = null

    @SerialName("DateOfBirth")
    var dateOfBirth: String? = null

    @SerialName("GeoLocation")
    var geoLocation: String? = null
}
