package com.creditclub.core.data.request

import com.creditclub.core.data.model.SurveyAnswer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitSurveyRequest(
    @SerialName("AgentPhoneNumber")
    val agentPhoneNumber: String? = "",

    @SerialName("InstitutionCode")
    val institutionCode: String? = "",

    @SerialName("GeoLocation")
    val geoLocation: String? = null,

    @SerialName("Answers")
    val answers: List<SurveyAnswer>? = null,
)