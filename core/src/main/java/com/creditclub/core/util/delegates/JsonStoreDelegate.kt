package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KProperty

class JsonStoreDelegate<T : Any>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val serializer: KSerializer<T>,
    private val json: Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = true
    }
) {
    operator fun getValue(obj: Any, prop: KProperty<*>): T? {
        val value = prefs.getString(key, null) ?: return null
        return json.decodeFromString(serializer, value)
    }

    operator fun setValue(obj: Any, prop: KProperty<*>, newValue: T?) {
        val serializedValue = if (newValue == null) null
        else json.encodeToString(serializer, newValue)

        prefs.edit {
            putString(key, serializedValue)
        }
    }
}

inline fun <reified T : Any> SharedPreferences.jsonStore(
    key: String,
    serializer: KSerializer<T>
): JsonStoreDelegate<T> {
    return JsonStoreDelegate(this, key, serializer)
}

inline fun <reified T : Any> SharedPreferences.jsonStore(key: String): JsonStoreDelegate<T> {
    return JsonStoreDelegate(this, key, serializer())
}