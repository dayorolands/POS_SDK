package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LogCaseRequest {
    @SerialName("InstitutionCode")
    var institutionCode = ""

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber = ""

    @SerialName("AgentPin")
    var agentPin = ""

    @SerialName("Product")
    var product = ""

    @SerialName("CaseCategoryID")
    var caseCategoryID = ""

    @SerialName("Subject")
    var subject = ""

    @SerialName("Description")
    var description = ""

    @SerialName("CaseReporterEmail")
    var caseReporterEmail = ""
}