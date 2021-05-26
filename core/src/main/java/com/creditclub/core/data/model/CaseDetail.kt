package com.creditclub.core.data.model

import android.os.Parcelable
import com.creditclub.core.data.InstantParceler
import com.creditclub.core.serializer.TimeInstantSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@Parcelize
@TypeParceler<Instant, InstantParceler>
@TypeParceler<Instant?, InstantParceler>
data class CaseDetail(
    @SerialName("Description")
    val description: String? = null,

    @SerialName("Subject")
    val subject: String,

    @SerialName("DateLogged")
    @Serializable(with = TimeInstantSerializer::class)
    val dateLogged: Instant,

    @SerialName("CaseReporterEmail")
    val caseReporterEmail: String? = null,

    @SerialName("CaseReference")
    val caseReference: String,

    @SerialName("CategoryName")
    val categoryName: String = "",

    @SerialName("InstitutionCode")
    val institutionCode: String = "",

    @SerialName("Product")
    val product: String = "",

    @SerialName("IsResolved")
    val isResolved: Boolean? = null,

    @SerialName("IsClosed")
    val isClosed: Boolean? = null,
) : Parcelable
