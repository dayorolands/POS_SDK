package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 15/11/2019.
 * Appzone Ltd
 */
@Serializable
class SurveyAnswer(
    @SerialName("QuestionID") var questionId: String = "",
    @SerialName("AnswerID") var answerId: String? = null,
    @SerialName("Rating") var rating: Float? = null
)