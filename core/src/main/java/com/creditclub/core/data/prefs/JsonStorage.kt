package com.creditclub.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.creditclub.core.BuildConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonDecodingException
import java.time.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 29/11/2019.
 * Appzone Ltd
 */

object JsonStorage {

    val json = Json(
        JsonConfiguration.Stable.copy(
            isLenient = true,
            ignoreUnknownKeys = true,
            serializeSpecialFloatingPointValues = true,
            useArrayPolymorphism = true
        )
    )

    fun getStore(context: Context): Store {
        return Store(context.getSharedPreferences("JSON_STORAGE", Context.MODE_PRIVATE))
    }

    data class JsonObject<T>(
        val data: T? = null,
        val updatedAt: Instant? = null
    )

    class Store(pref: SharedPreferences) : SharedPreferences by pref {

        fun <T> save(key: String, obj: T, serializer: KSerializer<T>) {
            val previousValue: String? = getString("DATA_${key}", null)
            val newValue = json.stringify(serializer, obj)
            if (BuildConfig.DEBUG) Log.d("JSONStorage", "Save $key: $newValue")

            if (previousValue != newValue) {
                edit {
                    putString("DATA_${key}", newValue)
                    putString("META_${key}_UPDATED_AT", Instant.now().toString())
                }
            }
        }

        fun delete(key: String) = edit {
            remove("DATA_${key}")
            remove("META_${key}_UPDATED_AT")
        }

        fun has(key: String): Boolean {
            return contains("DATA_${key}") || contains("META_${key}_UPDATED_AT")
        }

        fun <T> get(key: String, serializer: KSerializer<T>): JsonObject<T> {
            try {
                val data: String? = getString("DATA_${key}", null)
                val updatedAt: String? = getString("META_${key}_UPDATED_AT", null)

                if (data == null || updatedAt == null) {
                    return JsonObject()
                }

                if (BuildConfig.DEBUG) Log.d(
                    "JSONStorage",
                    "GET $key: $data // updated at: $updatedAt"
                )

                val obj = json.parse(serializer, data)

                return JsonObject(obj, Instant.parse(updatedAt))
            } catch (ex: JsonDecodingException) {
                if (BuildConfig.DEBUG) ex.printStackTrace()
                return JsonObject()
            }
        }
    }
}