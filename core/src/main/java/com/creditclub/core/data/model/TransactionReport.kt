package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionReport(
    @SerialName("Reports")
    val reports: List<ReportItem>? = null,

    @SerialName("totalCount")
    val totalCount: Int = 0
) {
    @Serializable
    data class ReportItem(
        @SerialName("To")
        val to: String? = null,

        @SerialName("From")
        val from: String? = null,

        @SerialName("CustomerID")
        val customerID: String? = null,

        @SerialName("CustomerName")
        val customerName: String? = null,

        @SerialName("CustomerPhone")
        val customerPhone: String? = null,

        @SerialName("ProductName")
        val productName: String? = null,

        @SerialName("ProductCode")
        val productCode: String? = null,

        @SerialName("Amount")
        val amount: Double,

        @SerialName("Date")
        val date: String? = null,

        @SerialName("TransactionTypeID")
        val transactionTypeID: Int = 0,

        @SerialName("TransactionTypeName")
        val transactionTypeName: Int = 0,

        @SerialName("FromPhoneNumber")
        val fromPhoneNumber: String? = null,

        @SerialName("EncryptedPIN")
        val encryptedPIN: String? = null,

        @SerialName("STAN")
        val stan: String? = null,

        @SerialName("SwitchTransactionTime")
        val switchTransactionTime: String? = null,

        @SerialName("IsActive")
        val isActive: Boolean = false,

        @SerialName("DisplayMessage")
        val displayMessage: String? = null,

        @SerialName("UniqueReference")
        val uniqueReference: String,

        @SerialName("ID")
        val id: Long = 0
    )
}
