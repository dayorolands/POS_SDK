package com.globalaccelerex.nexgo.n3.models

import com.creditclub.pos.PosParameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ParameterObject : PosParameter.ManagementData {
    @SerialName("03")
    override var cardAcceptorId = ""

    @SerialName("05")
    override var currencyCode = ""

    @SerialName("06")
    override var countryCode = ""

    @SerialName("08")
    override var merchantCategoryCode = ""

    @SerialName("52")
    override var cardAcceptorLocation = ""
}