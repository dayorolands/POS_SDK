package com.cluster.core.data.model

import com.cluster.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class SubscriptionOption(
    @SerialName("FeeDiscount")
    val feeDiscount: Int = 0,
    @SerialName("ID")
    val id: Int = 0,
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
    val options: List<SubscriptionOption> = listOf(),
    @SerialName("ValidityPeriod")
    val validityPeriod: Int = 0
)

@Serializable
data class Subscription(
    @SerialName("InstitutionCode")
    val institutionCode: String = "",
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String = "",
    @SerialName("AgentCode")
    val agentCode: String = "",
    @SerialName("Plan")
    val plan: SubscriptionPlan,
    @SerialName("StartDate")
    @Serializable(with = TimeInstantSerializer::class)
    val startDate: Instant? = null,
    @SerialName("ExpiryDate")
    @Serializable(with = TimeInstantSerializer::class)
    val expiryDate: Instant? = null,
)

@Serializable
data class SubscriptionRequest(
    @SerialName("AgentPIN")
    val agentPin: String = "",
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String = "",
    @SerialName("InstitutionCode")
    val institutionCode: String = "",
    @SerialName("NewPlanID")
    val newPlanId: Int = 0,
    @SerialName("PlanID")
    val planId: Int = 0
)