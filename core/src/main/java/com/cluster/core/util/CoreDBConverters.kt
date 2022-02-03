package com.cluster.core.util

import androidx.room.TypeConverter
import java.time.Instant
import java.time.format.DateTimeFormatter


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/21/2019.
 * Appzone Ltd
 */

object CoreDBConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String?): Instant? {
        return value?.let {
            return Instant.from(formatter.parse(value))
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(date: Instant?): String? {
        return date?.toString()
    }
}