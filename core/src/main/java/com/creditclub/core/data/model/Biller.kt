package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Biller {

    @SerialName("Description")
    var description: String? = null

    @SerialName("IsAirtime")
    var isAirtime = true

    @SerialName("BillerCategoryID")
    var billerCategoryID = 0

    @SerialName("ID")
    var id: String? = null

    @SerialName("Name")
    var name: String? = null

    @SerialName("Status")
    var status = true

    @SerialName("StatusDetails")
    var statusDetails: String? = null

    @SerialName("RequestStatus")
    var requestStatus = true

    @SerialName("ResponseDescription")
    var responseDescription: String? = null

    @SerialName("ResponseStatus")
    var responseStatus: String? = null

    @SerialName("Amount")
    var amount: String? = null
}
