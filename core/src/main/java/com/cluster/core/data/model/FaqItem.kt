package com.cluster.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class FaqItem(
    @SerialName("Question")
    val question: String = "",

    @SerialName("Answer")
    val answer: String = "",
) : Parcelable