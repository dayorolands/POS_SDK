package com.creditclub.core.data.model

import com.creditclub.core.serializer.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Period
import kotlin.math.max

@Serializable
data class AppVersion(

    @SerialName("Link")
    val link: String,

    @SerialName("Version")
    val version: String,

    @SerialName("GracePeriod")
    val gracePeriod: Int? = null,
) {

    @SerialName("NotifiedAt")
    @Serializable(with = LocalDateSerializer::class)
    var notifiedAt: LocalDate = LocalDate.now()

    fun updateIsRequired(currentVersion: String): Boolean {
        return gracePeriod != null && updateIsAvailable(currentVersion) && daysOfGraceLeft() < 1
    }

    fun updateIsAvailable(currentVersion: String): Boolean {
        return Version(version ?: "0") > Version(currentVersion)
    }

    fun daysOfGraceLeft(): Int =
        (gracePeriod ?: 365) - Period.between(notifiedAt, LocalDate.now()).days
}

class Version(private val version: String) : Comparable<Version?> {

    fun get() = version

    override operator fun compareTo(other: Version?): Int {
        if (other == null) return 1
        val thisParts = this.get().split("\\.".toRegex()).toTypedArray()
        val thatParts = other.get().split("\\.".toRegex()).toTypedArray()
        val length = max(thisParts.size, thatParts.size)
        for (i in 0 until length) {
            val thisPart = if (i < thisParts.size) thisParts[i].toInt() else 0
            val thatPart = if (i < thatParts.size) thatParts[i].toInt() else 0
            if (thisPart < thatPart) return -1
            if (thisPart > thatPart) return 1
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        return if (this.javaClass != other.javaClass) false else this.compareTo(other as Version) == 0
    }

    init {
        require(version.matches(Regex("[0-9]+(\\.[0-9]+)*"))) { "Invalid version format" }
    }
}