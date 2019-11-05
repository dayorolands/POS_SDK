package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BVNDetails {
    @SerialName("BVN")
    var bvn = ""

    @SerialName("OtherNames")
    var otherNames = ""

    @SerialName("PhoneNumber")
    var phoneNumber = ""

    @SerialName("FirstName")
    var firstName = ""

    @SerialName("LastName")
    var lastName = ""

    @SerialName("DOB")
    var dob = ""
}