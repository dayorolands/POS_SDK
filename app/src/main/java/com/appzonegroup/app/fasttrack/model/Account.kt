package com.appzonegroup.app.fasttrack.model

class Account {
    var iD: Long = 0
    var gender: String? = null
    var phoneNo: String? = null
    var lastName: String? = null
    var firstName: String? = null
    var email: String? = null
    var state: String? = null
    var dateOfBirth: String? = null
    var address: String? = null
    var placeOfBirth: String? = null
    var bvn: String? = null
    var productCode: String? = null

    val customer: Customer
        get() = Customer(
            productCode = productCode,
            productName = "",
            accountNumber = "4567890987",
            pin = "",
            customerLastName = lastName,
            customerFirstName = firstName,
            customerPhoneNumber = phoneNo,
            gender = gender,
            bvn = bvn,
            address = address,
            placeOfBirth = placeOfBirth,
            dateOfBirth = dateOfBirth,
        )
}