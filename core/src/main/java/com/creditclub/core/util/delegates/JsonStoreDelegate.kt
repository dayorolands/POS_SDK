package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.reflect.KProperty

class JsonStoreDelegate<T : Any>(
    private val key: String,
    private val serializer: KSerializer<T>,
    private val json: Json = Json(JsonConfiguration.Stable)
) {
    operator fun getValue(pref: SharedPreferences, prop: KProperty<*>): T? {
        val value = pref.getString(key, null) ?: return null
        return json.parse(serializer, value)
    }

    operator fun setValue(pref: SharedPreferences, prop: KProperty<*>, newValue: T?) {
        val serializedValue = if (newValue == null) null
        else json.stringify(serializer, newValue)

        pref.edit {
            putString(key, serializedValue)
        }
    }
}

inline fun <reified T : Any> jsonStore(
    key: String,
    serializer: KSerializer<T>
): JsonStoreDelegate<T> {
    return JsonStoreDelegate(key, serializer)
}