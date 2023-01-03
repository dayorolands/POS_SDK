package com.cluster.core.data.request

import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/21/2019.
 * Appzone Ltd
 */
@Serializable
data class POSTransactionReportRequest(
    val agentPhoneNumber: String? = "",
    val institutionCode: String? = "",
    val from: String? = "",
    val to: String? = "",
    val startIndex: String? = "",
    val maxSize: String? = "",
    val status: Int? = 3,
)

@Serializable
data class PWTTransactionReportRequest(
    val agentPhoneNumber: String? = "",
    val institutionCode: String? = "",
    val agentCode: String? = "",
    val from: String? = "",
    val to: String? = "",
    val startIndex: String? = "",
    val maxSize: String? = "",
    val status: Int? = 3,
)