package com.cluster.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MiniStatementRequest(
    @SerialName("AccountNumber")
    var accountNumber: String? = null,

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null,

    @SerialName("AgentPin")
    var agentPin: String? = null,

    @SerialName("StartDate")
    var startDate: String? = null,

    @SerialName("EndDate")
    var endDate: String? = null,

    @SerialName("TransactionCount")
    var transactionCount: Int = 0,

    @SerialName("InstitutionCode")
    var institutionCode: String? = null,

    @SerialName("GeoLocation")
    var geoLocation: String? = null,
)