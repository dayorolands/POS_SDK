package com.creditclub.core.data.model

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
    @SerialName("Status")
    val status: String,
) {
    companion object {
        const val Success = "0"
        const val Failed = "1"
        const val Pending = "2"
    }
}