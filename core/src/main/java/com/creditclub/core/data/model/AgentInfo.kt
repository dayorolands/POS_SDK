package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AgentInfo {

    @SerialName("Message")
    var message: String? = null

    @SerialName("Status")
    var status: Boolean = false

    @SerialName("AgentName")
    var agentName: String? = null

    @SerialName("AgentCode")
    var agentCode: String? = null

    @SerialName("PhoneNumber")
    var phoneNumber: String? = null

    @SerialName("TerminalID")
    var terminalID: String? = null

    @SerialName("PosMode")
    var posMode: String? = null

    @SerialName("CardLimit")
    var cardLimit: Double? = null
}
