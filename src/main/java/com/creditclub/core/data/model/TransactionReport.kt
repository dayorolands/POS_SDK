package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TransactionReport {

    @SerialName("Reports")
    var reports: Array<ReportItem>? = null

    @SerialName("totalCount")
    var totalCount: Int = 0

    @Serializable
    class ReportItem {

        @SerialName("To")
        var to: String? = null

        @SerialName("From")
        var from: String? = null

        @SerialName("CustomerID")
        var customerID: String? = null

        @SerialName("CustomerName")
        var customerName: String? = null

        @SerialName("CustomerPhone")
        var customerPhone: String? = null

        @SerialName("ProductName")
        var productName: String? = null

        @SerialName("ProductCode")
        var productCode: String? = null

        @SerialName("Amount")
        var amount: Double = 0.toDouble()

        @SerialName("Date")
        var date: String? = null

        @SerialName("TransactionTypeID")
        var transactionTypeID: Int = 0

        @SerialName("TransactionTypeName")
        var transactionTypeName: Int = 0

        @SerialName("FromPhoneNumber")
        var fromPhoneNumber: String? = null

        @SerialName("EncryptedPIN")
        var encryptedPIN: String? = null

        @SerialName("STAN")
        var stan: String? = null

        @SerialName("SwitchTransactionTime")
        var switchTransactionTime: String? = null

        @SerialName("IsActive")
        var isActive: Boolean = false

        @SerialName("DisplayMessage")
        var displayMessage: String? = null

        @SerialName("ID")
        var id: Long = 0
    }
}
