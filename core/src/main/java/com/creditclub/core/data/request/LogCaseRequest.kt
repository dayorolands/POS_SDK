package com.creditclub.core.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LogCaseRequest {
    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("AgentPin")
    var agentPin: String? = null

    @SerialName("Product")
    var product: String? = null

    @SerialName("CaseCategoryID")
    var caseCategoryID: String? = null

    @SerialName("Subject")
    var subject: String? = null

    @SerialName("Description")
    var description: String? = null

    @SerialName("CaseReporterEmail")
    var caseReporterEmail: String? = null

    @SerialName("FCMToken")
    var fcmToken: String? = null

    @SerialName("Blob")
    var blob: MutableList<String?>? = null
}