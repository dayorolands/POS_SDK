package com.creditclub.core.data.request

import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/21/2019.
 * Appzone Ltd
 */
@Serializable
class POSTransactionReportRequest {
    var agentPhoneNumber: String? = ""
    var institutionCode: String? = ""
    var from: String? = ""
    var to: String? = ""
    var startIndex: String? = ""
    var maxSize: String? = ""
    var status: Int? = 3
}