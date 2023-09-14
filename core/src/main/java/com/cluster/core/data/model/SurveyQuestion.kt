package com.cluster.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
class SurveyQuestion(
    @SerialName("ID") var id: String = "0",
    @SerialName("Name") var name: String = "",
    @SerialName("Type") var type: SurveyQuestionType = SurveyQuestionType.Rating
) {

    @SerialName("Options")
    var options: List<Option>? = null

    @Serializable
    class Option(
        @SerialName("ID") var id: String = "",
        @SerialName("Text") var text: String = ""
    )
}

enum class SurveyQuestionType {
    Rating,
    MultipleChoice,
    Boolean
}