package com.creditclub.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.creditclub.core.BuildConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import kotlinx.serialization.serializerByTypeToken
import kotlinx.serialization.typeTokenOf
import org.threeten.bp.Instant
import kotlin.collections.set


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 29/11/2019.
 * Appzone Ltd
 */

object JsonStorage {

    val serializerCache = HashMap<String, KSerializer<*>>()

    fun getStore(context: Context): Store {
        return Store(context.getSharedPreferences("JSON_STORAGE", Context.MODE_PRIVATE))
    }

    data class JsonObject<T : Any>(
        val data: T? = null,
        val updatedAt: Instant? = null
    ) {
        val isValid get() = data != null
    }

    class Store(pref: SharedPreferences) : SharedPreferences by pref {

        @UnstableDefault
        inline fun <reified T : Any> save(key: String, obj: T) {

            val previousValue: String? = getString("DATA_${key}", null)
            val newValue = Json.nonstrict.stringify(getSerializer(), obj)
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

        @UnstableDefault
        inline fun <reified T : Any> get(key: String): JsonObject<T> {
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

                val obj = Json.nonstrict.parse(getSerializer<T>(), data)

                return JsonObject(obj, Instant.parse(updatedAt))
            } catch (ex: JsonDecodingException) {
                if (BuildConfig.DEBUG) ex.printStackTrace()
                return JsonObject()
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "NO_REFLECTION_IN_CLASS_PATH")
    inline fun <reified T> getSerializer(): KSerializer<T> {
        val key = typeTokenOf<T>().typeName
        if (!serializerCache.containsKey(key)) {
            serializerCache[key] = serializerByTypeToken(typeTokenOf<T>())
        }
        return serializerCache[key] as KSerializer<T>
    }
}