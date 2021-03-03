package com.appzonegroup.creditclub.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.creditclub.core.serializer.TimeInstantSerializer
import com.creditclub.pos.model.ConnectionInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.KoinComponent
import java.time.Instant

@Serializable
data class DisputedPosTransaction(
    @SerialName("ID")
    val id: Int = 0,

    @SerialName("DisputeReference")
    val disputeReference: String? = null,

    @SerialName("DisputeStatus")
    val disputeStatus: Int = 0,

    @SerialName("IssuingBankName")
    val issuingBankName: String? = null,

    @SerialName("CustomerPhoneNumber")
    val customerPhoneNumber: String? = null,

    @SerialName("CustomerFirstName")
    val customerFirstName: String? = null,

    @SerialName("CustomerLastName")
    val customerLastName: String? = null,

    @SerialName("CustomerEmail")
    val customerEmail: String? = null,

    @SerialName("AgentName")
    val agentName: String? = null,

    @SerialName("AgentCode")
    val agentCode: String? = null,

    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = null,

    @SerialName("InstitutionCode")
    val institutionCode: String? = null,

    @SerialName("PAN")
    val pan: String? = null,

    @SerialName("TerminalID")
    val terminalId: String? = null,

    @SerialName("TransactionType")
    val transactionType: String? = null,

    @SerialName("STAN")
    val stan: String? = null,

    @SerialName("Amount")
    val amount: Double = 0.0,

    @SerialName("CardType")
    val cardType: String? = null,

    @SerialName("ExpiryDate")
    val expiryDate: String? = null,

    @SerialName("ResponseCode")
    val responseCode: String? = null,

    @SerialName("RetrievalReferenceNumber")
    val retrievalReferenceNumber: String? = null,

    @SerialName("AppName")
    val appName: String? = null,

    @SerialName("PTSP")
    val ptsp: String? = null,

    @SerialName("Website")
    val website: String? = null,

    @SerialName("MerchantDetails")
    val merchantDetails: String? = null,

    @SerialName("MerchantID")
    val merchantId: String? = null,

    @SerialName("CardHolder")
    val cardHolder: String? = null,

    @SerialName("NodeName")
    val nodeName: String? = null,

    @SerialName("TransactionDate")
    @Serializable(with = TimeInstantSerializer::class)
    val transactionDate: Instant? = Instant.now(),
)

fun DisputedPosTransaction.Companion.from(posTransaction: PosTransaction) = posTransaction.run {
    DisputedPosTransaction(
        agentName = agentName,
        agentCode = agentCode,
        agentPhoneNumber = agentPhoneNumber,
        institutionCode = institutionCode,
        pan = pan,
        terminalId = terminalId,
        transactionType = transactionType,
        stan = stan,
        amount = amount?.toDoubleOrNull() ?: 0.0,
        cardType = cardType,
        expiryDate = expiryDate,
        responseCode = responseCode,
        retrievalReferenceNumber = retrievalReferenceNumber,
        appName = appName,
        ptsp = ptsp,
        website = website,
        merchantDetails = merchantDetails,
        merchantId = merchantId,
        cardHolder = cardHolder,
        nodeName = nodeName,
        transactionDate = dateTime,
    )
}
