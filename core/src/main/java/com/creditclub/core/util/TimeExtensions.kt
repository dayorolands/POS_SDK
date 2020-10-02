package com.creditclub.core.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */

private const val defaultZone = "UTC+01:00"
const val CREDIT_CLUB_DATE_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.SSS][xxx][xx][X]"

private fun formatter(pattern: String, zoneID: String = defaultZone): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(pattern).withLocale(Locale.ENGLISH)
        .withZone(ZoneId.of(zoneID))
}

val Date.longString: String
    get() {
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS")
        var part = formatter.format(this)
        part += "0000 "
        return part + SimpleDateFormat("a").format(this)
    }

fun Instant.format(pattern: String, zoneID: String = defaultZone): String {
    return formatter(pattern, zoneID).format(this)
}

fun Instant.toOffsetDateTime(): String {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this)
}

fun Instant.toString(pattern: String, zoneID: String = defaultZone) = format(pattern, zoneID)

fun LocalDate.format(pattern: String, zoneID: String = defaultZone): String {
    return formatter(pattern, zoneID).format(this)
}

fun LocalDate.toString(pattern: String, zoneID: String = defaultZone) = format(pattern, zoneID)

fun String.toLocalDate() = LocalDate.parse(this)

fun String.toInstant(pattern: String, zoneID: String = defaultZone): Instant {
    return Instant.from(formatter(pattern, zoneID).parse(this))
}

fun String.toInstant(formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME): Instant {
    return Instant.from(formatter.parse(this))
}

fun Instant.timeAgo(): String {
    val duration = Duration.between(this, Instant.now())
    return TimeAgo.toDuration(duration.seconds)
}