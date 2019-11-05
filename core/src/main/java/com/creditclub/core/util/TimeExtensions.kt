package com.creditclub.core.util

import android.util.Log
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */

private const val defaultZone = "UTC+01:00"
const val CREDIT_CLUB_DATE_PATTERN = "uuuu-MM-dd'T'HH:mm:ss[.SSS][xxx][xx][X]"

private fun formatter(pattern: String, zoneID: String = defaultZone): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(pattern).withLocale(Locale.ENGLISH).withZone(ZoneId.of(zoneID))
}

val Date.shortString: String
    get() {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.format(this)
    }

fun dateToShortStringDDMMYYYY(date: Date): String {
    val formatter = SimpleDateFormat("dd-MM-yyyy")
    return formatter.format(date)
}

val Date.longString: String
    get() {
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS")
        var part = formatter.format(this)
        part += "0000 "
        return part + SimpleDateFormat("a").format(this)
    }

fun dateToLongStringNoMicrosecond(date: Date): String {
    return SimpleDateFormat("dd-MMM-yyyy hh:mm:ss").format(date)
}

fun stringToDate(dateString: String): Date {
    try {
        return SimpleDateFormat("yyyy-MM-dd").parse(dateString)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    return Date()
}

fun serverTimetoDate(time: String): Date {
    try {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(time)
    } catch (e: ParseException) {
        e.printStackTrace()
        Log.e("custom", "unable to parse time")
    }

    return Date()
}

fun Instant.toString(pattern: String, zoneID: String = defaultZone): String {
    return formatter(pattern, zoneID).format(this)
}

fun LocalDate.toString(pattern: String, zoneID: String = defaultZone): String {
    return formatter(pattern, zoneID).format(this)
}

fun String.toInstant(pattern: String, zoneID: String = defaultZone): Instant {
    return Instant.from(formatter(pattern, zoneID).parse(this))
}

fun String.toInstant(): Instant {
    return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this))
}

fun Instant.timeAgo(): String {
    val duration = Duration.between(this, Instant.now())
    return TimeAgo.toDuration(duration.seconds)
}