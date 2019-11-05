package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Joseph on 6/6/2016.
 */
@Serializable
class Product {

    @SerialName("ID")
    var id: Long = 0

    @SerialName("Name")
    var name: String = ""

    @SerialName("Code")
    var code: String = ""

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = null
}
