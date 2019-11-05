package com.appzonegroup.creditclub.pos.util

import androidx.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/21/2019.
 * Appzone Ltd
 */

object RoomConverters {
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