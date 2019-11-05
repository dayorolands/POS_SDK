package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 10/09/2019.
 * Appzone Ltd
 */

@Serializable
class PosTransactionReport {

    @SerialName("Report")
    var reports: List<Report>? = null

    @SerialName("totalCount")
    var totalCount = 0

    @Serializable
    class Report {
        @SerialName("AgentAccountNumber")
        var agentAccountNumber = "0701071736"

        @SerialName("AgentPhoneNumber")
        var agentPhoneNumber = "08026319666"

        @SerialName("TransactionAmount")
        var transactionAmount = 1000

        @SerialName("CustomerAccountNumber")
        var customerAccountNumber: String? = null

        @SerialName("CustomerPhoneNumber")
        var customerPhoneNumber = "08026319879"

        @SerialName("DateLogged")
        var dateLogged = "2018-04-12T07=00=17"

        @SerialName("ID")
        var id = 0

        @SerialName("SettlementDate")
        var settlementDate: String? = null

        @SerialName("Status")
        var status = 1

        @SerialName("TransactionReference")
        var transactionReference = "a359d058-4c6c-4812-bb0a-015a19de7e17"

        @SerialName("AmountCreditedToAgent")
        var amountCreditedToAgent = 0

        @SerialName("IsASystemChange")
        var isASystemChange = false
    }
}