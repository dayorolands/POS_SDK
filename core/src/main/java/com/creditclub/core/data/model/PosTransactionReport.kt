package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 10/09/2019.
 * Appzone Ltd
 */

@Serializable
class PosTransactionReport {

    @SerialName("Reports")
    var reports: List<Report>? = null

    @SerialName("totalCount")
    var totalCount = 0

    @SerialName("totalAmount")
    var totalAmount = 0.0

    @Serializable
    class Report {
        @SerialName("AgentAccountNumber")
        var agentAccountNumber = ""

        @SerialName("AgentPhoneNumber")
        var agentPhoneNumber = ""

        @SerialName("TransactionAmount")
        var transactionAmount: Double = 0.0

        @SerialName("CustomerAccountNumber")
        var customerAccountNumber: String? = null

        @SerialName("CustomerPhoneNumber")
        var customerPhoneNumber: String? = null

        @SerialName("DateLogged")
        var dateLogged = ""

        @SerialName("ID")
        var id = 0

        @SerialName("SettlementDate")
        var settlementDate: String? = null

        @SerialName("Status")
        var status = 1

        @SerialName("TransactionReference")
        var transactionReference = ""

        @SerialName("AmountCreditedToAgent")
        var amountCreditedToAgent: Double = 0.0

        @SerialName("IsASystemChange")
        var isASystemChange = false
    }
}