package com.appzonegroup.creditclub.pos.util

import java.time.Instant
import java.time.format.DateTimeFormatter


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/22/2019.
 * Appzone Ltd
 */

class DateFormat(val pattern: String) {
    private val formatter by lazy { DateTimeFormatter.ISO_OFFSET_DATE_TIME }

    fun toDate(value: String): Instant {
        return Instant.from(formatter.parse(value))
    }
//
//    fun fromDate(date: Instant): String {
//        var value = ""
//        formatter.formatTo(date, value)
//        return value
//    }
}