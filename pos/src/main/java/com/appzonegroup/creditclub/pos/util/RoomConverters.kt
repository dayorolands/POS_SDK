package com.appzonegroup.creditclub.pos.util

import androidx.room.TypeConverter
import com.creditclub.pos.model.ConnectionInfo
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.format.DateTimeFormatter

object RoomConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = true
        encodeDefaults = true
    }

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
            return json.decodeFromString(ConnectionInfo.serializer(), this)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromConnectionInfo(connectionInfo: ConnectionInfo?): String? {
        return connectionInfo?.run {
            json.encodeToString(ConnectionInfo.serializer(), this)
        }
    }
}