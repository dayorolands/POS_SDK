package com.creditclub.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/11/2019.
 * Appzone Ltd
 */
@Serializable
class FaqItem {
    @SerialName("Question")
    var question: String = ""

    @SerialName("Answer")
    var answer: String = ""
}