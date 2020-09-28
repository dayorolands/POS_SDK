package com.appzonegroup.creditclub.pos.util

import androidx.room.TypeConverter
import com.creditclub.pos.model.ConnectionInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.time.Instant
import java.time.format.DateTimeFormatter


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/21/2019.
 * Appzone Ltd
 */

object RoomConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val json = Json(
        JsonConfiguration.Stable.copy(
            isLenient = true,
            ignoreUnknownKeys = true,
            serializeSpecialFloatingPointValues = true,
            useArrayPolymorphism = true
        )
    )

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

    @TypeConverter
    @JvmStatic
    fun toConnectionInfo(value: String?): ConnectionInfo? {
        return value?.run {
            return json.parse(ConnectionInfo.serializer(), this)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromConnectionInfo(connectionInfo: ConnectionInfo?): String? {
        return connectionInfo?.run {
            json.stringify(ConnectionInfo.serializer(), this)
        }
    }
}