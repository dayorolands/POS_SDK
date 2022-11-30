package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AgentInfo(
    @SerialName("Message")
    val message: String? = null,

    @SerialName("Status")
    val status: Boolean = false,

    @SerialName("AgentName")
    val agentName: String? = null,

    @SerialName("AgentCode")
    val agentCode: String? = null,

    @SerialName("USSDCode")
    val ussdCode: String? = null,

    @SerialName("PhoneNumber")
    val phoneNumber: String? = null,

    @SerialName("TerminalID")
    val terminalID: String? = null,

    @SerialName("POSMode")
    val posMode: String? = null,

    @SerialName("CardLimit")
    val cardLimit: Double? = null,

    @SerialName("AgentCategory")
    val agentCategory: Int = 0
)