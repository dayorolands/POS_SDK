package com.creditclub.core.data.request

import com.creditclub.core.data.model.SurveyAnswer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 22/11/2019.
 * Appzone Ltd
 */
@Serializable
class SubmitSurveyRequest {
    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = ""

    @SerialName("InstitutionCode")
    var institutionCode: String? = ""

    @SerialName("GeoLocation")
    var geoLocation: String? = null

    @SerialName("Answers")
    var answers: List<SurveyAnswer>? = null
}