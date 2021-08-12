package com.creditclub.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */

@Serializable
open class BackendResponse {
    @SerialName("ReponseMessage")
    var responseMessage: String? = null
        get() = if (field.isNullOrEmpty()) message else field

    @SerialName("IsSuccessful")
    var isSuccessful: Boolean = false

    @SerialName("Message")
    private val message: String? = null

    @SerialName("ResponseCode")
    val responseCode: String? = null

    fun isFailure() = !isSuccessful
    fun isPendingOnBank() = responseCode == "24"
    fun isPending() = responseCode == "20" && isPendingOnBank()
    fun isSuccess() = isSuccessful
}

@Serializable
open class BackendResponseWithCode : BackendResponse() {
    @SerialName("TransactionSequenceNumber")
    val transactionSequenceNumber: Long = 0
}