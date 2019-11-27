package com.creditclub.core.data.model

import com.creditclub.core.serializer.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

@Serializable
data class AppVersion(

    @SerialName("Link")
    var link: String? = null,

    @SerialName("Version")
    var version: String? = null,

    @SerialName("GracePeriod")
    var gracePeriod: String? = null
) {

    @SerialName("NotifiedAt")
    @Serializable(with = LocalDateSerializer::class)
    var notifiedAt: LocalDate = LocalDate.now()

    fun updateRequired(currentVersion: String): Boolean {
        return canUpdate(currentVersion) && daysOfGraceLeft() < 0
    }

    fun canUpdate(currentVersion: String): Boolean {
        return version ?: "0" > currentVersion
    }

    fun daysOfGraceLeft(): Int = Period.between(notifiedAt, LocalDate.now()).days
}