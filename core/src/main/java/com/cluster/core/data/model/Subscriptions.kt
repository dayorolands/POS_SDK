package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Option(
    @SerialName("FeeDiscount")
    val feeDiscount: Int = 0,
    @SerialName("ID")
    val iD: Int = 0,
    @SerialName("MaximumBenefitVolume")
    val maximumBenefitVolume: Int = 0,
    @SerialName("TransactionType")
    val transactionType: Int = 0
)

@Serializable
data class SubscriptionPlan(
    @SerialName("DateCreated")
    val dateCreated: String = "",
    @SerialName("DateUpdated")
    val dateUpdated: String = "",
    @SerialName("Description")
    val description: String = "",
    @SerialName("DisplayMessage")
    val displayMessage: String? = null,
    @SerialName("Fee")
    val fee: Int = 0,
    @SerialName("ID")
    val id: Int = 0,
    @SerialName("InstitutionCode")
    val institutionCode: String = "",
    @SerialName("IsActive")
    val isActive: Boolean = false,
    @SerialName("Name")
    val name: String = "",
    @SerialName("Options")
    val options: List<Option> = listOf(),
    @SerialName("ValidityPeriod")
    val validityPeriod: Int = 0
)