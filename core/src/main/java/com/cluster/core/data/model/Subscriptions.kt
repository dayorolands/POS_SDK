package com.cluster.core.data.model

import com.cluster.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class SubscriptionPlan(
    @SerialName("DateCreated")
    @Serializable(with = TimeInstantSerializer::class)
    val dateCreated: Instant,
    @SerialName("DateUpdated")
    @Serializable(with = TimeInstantSerializer::class)
    val dateUpdated: Instant,
    @SerialName("Description")
    val description: String,
    @SerialName("DisplayMessage")
    val displayMessage: String?,
    @SerialName("Fee")
    val fee: Double,
    @SerialName("ID")
    val id: Int,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("IsActive")
    val isActive: Boolean,
    @SerialName("Name")
    val name: String,
    @SerialName("ValidityPeriod")
    val validityPeriod: Int,
)

@Serializable
data class Subscription(
    @SerialName("ID")
    val id: Int,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("AgentCode")
    val agentCode: String,
    @SerialName("Plan")
    val plan: SubscriptionPlan,
    @SerialName("StartDate")
    @Serializable(with = TimeInstantSerializer::class)
    val startDate: Instant,
    @SerialName("ExpiryDate")
    @Serializable(with = TimeInstantSerializer::class)
    val expiryDate: Instant,
    @SerialName("AutoRenew")
    val autoRenew: Boolean,
)

@Serializable
data class SubscriptionRequest(
    @SerialName("AgentPIN")
    val agentPin: String,
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("PlanID")
    val planId: Int,
    @SerialName("AutoRenew")
    val autoRenew: Boolean
)

@Serializable
data class SubscriptionMilestone(
    @SerialName("ID")
    val id: Int,
    @SerialName("TargetVolumeLeft")
    val targetVolumeLeft: Double,
    @SerialName("TargetVolumeMaxLimit")
    val targetVolumeMaxLimit: Double,
    @SerialName("TransactionType")
    val transactionType: Int,
    @SerialName("TargetCountLeft")
    val targetCountLeft: Int,
)

@Serializable
data class ChangeSubscriptionRequest(
    @SerialName("PlanID")
    val planId: Int,
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("AgentPin")
    val agentPin: String,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("AutoRenew")
    val autoRenew: Boolean,
)

@Serializable
data class RenewSuscriptionRequest(
    @SerialName("ID")
    val id: Int,
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String,
    @SerialName("AgentPin")
    val agentPin: String,
    @SerialName("InstitutionCode")
    val institutionCode: String,
    @SerialName("AutoRenew")
    val autoRenew: Boolean,
    @SerialName("Renew")
    val renew: Boolean,
)

@Serializable
data class SubscriptionFee(
    @SerialName("Fee")
    val fee: Double
)