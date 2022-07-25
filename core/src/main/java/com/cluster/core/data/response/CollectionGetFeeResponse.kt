package com.cluster.core.data.response

import com.cluster.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
class CollectionGetFeeResponse {
    @SerialName("ResponseMessage")
    var responseMessage: String? = null

    @SerialName("Data")
    var result: Double? = null

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean? = false

}