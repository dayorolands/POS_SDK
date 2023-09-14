package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class SurveyAnswer(
    @SerialName("QuestionID") var questionId: String = "",
    @SerialName("AnswerID") var answerId: String? = null,
    @SerialName("Rating") var rating: Float? = null
)