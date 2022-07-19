package com.cluster.core.data.model

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
        var status : String? = ""

        @SerialName("TransactionReference")
        var transactionReference = ""

        @SerialName("AmountCreditedToAgent")
        var amountCreditedToAgent: Double = 0.0

        @SerialName("IsASystemChange")
        var isASystemChange = false

        @SerialName("PAN")
        var maskedPan: String? = ""

        @SerialName("STAN")
        var transactionStan: String? = ""

        @SerialName("CardType")
        var cardType: String? = ""

        @SerialName("ExpiryDate")
        var expiryDate: String? = ""

        @SerialName("RetrievalReferenceNumber")
        var retrievalReferenceNumber: String? = ""

        @SerialName("CardHolder")
        var cardHolderName: String? = ""

        @SerialName("DateTime")
        var transactionDateTime: String? = ""
    }
}