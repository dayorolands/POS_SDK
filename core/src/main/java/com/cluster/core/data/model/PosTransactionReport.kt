package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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

        @SerialName("ResponseCode")
        var responseCode: String? = ""

        @SerialName("DateTime")
        var transactionDateTime: String? = ""
    }
}

@Serializable
class PWTTransactionReportResponse{
    @SerialName("Reports")
    var payWithTransferReport : List<PayWithTransferReport>? = null

    @SerialName("totalCount")
    var totalCount = 0

    @Serializable
    class PayWithTransferReport{
        @SerialName("VirtualAccount")
        var virtualAccountNumber: String? = ""
        @SerialName("AgentPhoneNumber")
        var agentPhoneNumber: String? = ""
        @SerialName("AgentCode")
        var agentCode : String? = ""
        @SerialName("Narration")
        var narration : String? = ""
        @SerialName("CustomerAccountName")
        var customerAccountName : String = ""
        @SerialName("CustomerAccountNumber")
        var customerAcctNumber : String? = ""
        @SerialName("AmountReceived")
        var amountReceived: Double = 0.00
        @SerialName("CustomerName")
        var customerName: String? = ""
        @SerialName("AgentAccountName")
        var agentAccountName: String = ""
        @SerialName("Date")
        var date: String? = ""
        @SerialName("RRN")
        var rrn: String? = ""
        @SerialName("ExpectedAmount")
        var expectedAmount: Double = 0.00
        @SerialName("AgentAccount")
        var agentAccount : String = ""
    }
}