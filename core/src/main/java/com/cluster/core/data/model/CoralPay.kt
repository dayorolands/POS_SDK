package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoraPayReference(
    @SerialName("RequestReference")
    val requestReference: String,
    @SerialName("TransactionReference")
    val transactionReference: String,
)

@Serializable
data class CoraPayTransactionStatus(
    @SerialName("Data")
    val data: Int,
) {
    companion object {
        const val Pending = 1
        const val Failed = 2
        const val Successful = 3
        const val Reversed = 4
        const val ThirdPartyFailure = 5
        const val NotFound = 6
    }
}