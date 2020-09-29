package com.globalaccelerex.nexgo.n3.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ParameterResponse {
    @SerialName("MerchantID")
    var merchantId: String? = null

    @SerialName("TerminalID")
    var terminalId: String? = null

    @SerialName("serialNumber")
    var serialNumber: String? = null

    @SerialName("PTSP")
    var ptsp: String? = null

    @SerialName("FooterMessage")
    var footerMessage: String? = null

    @SerialName("State")
    var state: String? = null

    @SerialName("MerchantName")
    var merchantName: String? = null

    @SerialName("BankName")
    var bankName: String? = null

    @SerialName("City")
    var city: String? = null
}