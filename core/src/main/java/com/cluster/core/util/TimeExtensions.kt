package com.cluster.core.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val defaultZone = "+0100"
const val CREDIT_CLUB_DATE_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.SSSSSSS][.SSSSSS][.SSS][xxx][xx][X]"
const val CREDIT_CLUB_REQUEST_DATE_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.SSS]"

private fun formatter(pattern: String, zoneID: String = defaultZone): DateTimeFormatter {
    return DateTimeFormatter
        .ofPattern(pattern)
        .withLocale(Locale.ENGLISH)
        .withZone(ZoneId.of(zoneID))
}

fun Instant.format(pattern: String, zoneID: String = defaultZone): String {
    return formatter(pattern, zoneID).format(this)
}

fun Instant.toString(pattern: String, zoneID: String = defaultZone) = format(pattern, zoneID)

fun LocalDate.format(pattern: String, zoneID: String = defaultZone): String {
    return formatter(pattern, zoneID).format(this)
}

fun LocalDate.toString(pattern: String, zoneID: String = defaultZone) = format(pattern, zoneID)

fun String.toInstant(pattern: String, zoneID: String = defaultZone): Instant {
    return Instant.from(formatter(pattern, zoneID).parse(this))
}

fun String.toInstant(): Instant {
    return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this))
}

fun String.instantFromPattern(pattern: String): Instant {
    return Instant.from(formatter(pattern).parse(this))
}

fun Instant.timeAgo(): String {
    val duration = Duration.between(this, Instant.now())
    return TimeAgo.toDuration(duration.seconds)
}