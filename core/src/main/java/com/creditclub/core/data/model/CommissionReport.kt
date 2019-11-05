package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CommissionReport {
    @SerialName("Reports")
    var reports: List<Report>? = null

    @SerialName("totalCount")
    var totalCount = 0

    @Serializable
    class Report {
        @SerialName("CommissionAmount")
        var commissionAmount: Double = 0.toDouble()

        @SerialName("DatePaid")
        var datePaid: String? = null

        @SerialName("PaymentStatus")
        var paymentStatus: Int = 0

        @SerialName("LogDate")
        var logDate: String = ""

        @SerialName("TransactionAmount")
        var transactionAmount: Double = 0.toDouble()

        @SerialName("TransactionType")
        var transactionType: Int = 1

        @SerialName("SettlementDate")
        var settlementDate: String? = null

        @SerialName("IsActive")
        var isActive: Boolean = false

        @SerialName("DisplayMessage")
        var displayMessage: String? = null

        @SerialName("ID")
        var id: Long = 0

        @SerialName("IsASystemChange")
        var isASystemChange: Boolean = false
    }
}