package com.cluster.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/11/2019.
 * Appzone Ltd
 */
@Serializable
@Parcelize
data class FaqItem(
    @SerialName("Question")
    val question: String = "",

    @SerialName("Answer")
    val answer: String = "",
) : Parcelable