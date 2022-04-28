package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponsePayload(
    @SerialName("Loan")
    val loanEligibility: AgentLoanEligibility? = null,
)
