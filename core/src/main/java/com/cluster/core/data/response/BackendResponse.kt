package com.cluster.core.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */

enum class RetryPolicy {
    AutoRetry,
    ManualRetry,
    RetryLater,
}

interface BackendResponseContract {
    val responseMessage: String?
    val isSuccessful: Boolean
}

@Serializable
open class BackendResponse : BackendResponseContract {
    @SerialName("ReponseMessage")
    override val responseMessage: String? = null
        get() = if (field.isNullOrEmpty()) message else field

    @SerialName("IsSuccessful")
    override var isSuccessful: Boolean = false

    @SerialName("Message")
    private val message: String? = null

    @SerialName("ResponseCode")
    val responseCode: String? = null

    fun isFailure() = !isSuccessful
    fun isPendingOnBank() = responseCode == "24"
    fun isPendingOnMiddleware() = responseCode == "20"
    fun isPending() = isPendingOnMiddleware() || isPendingOnBank()
    fun isSuccess() = isSuccessful
}

@Serializable
class AgentActivationResponse(
    @SerialName("TransactionSequenceNumber")
    val transactionSequenceNumber: Long = 0,

    @SerialName("DeviceNumber")
    val deviceNumber: Int = 0,
) : BackendResponse()

@Serializable
class BackendResponseWithPayload<T>(
    @SerialName("Data")
    val data: T? = null,
) : BackendResponse()
