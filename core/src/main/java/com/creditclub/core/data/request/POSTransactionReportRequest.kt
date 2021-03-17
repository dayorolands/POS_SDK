package com.creditclub.core.data.request

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