package com.creditclub.core.data.contract

import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */
interface IISoRequestLog {

    var id: Long

    var uniqueId: String

    var institutionCode: String

    var terminalId: String

    var rrn: String

    var transactionType: String

    var amount: String

    var agentCode: String

    var gpsCoordinates: String

    var responseCode: String

    var requestTime: Instant

    var responseTime: Instant
}