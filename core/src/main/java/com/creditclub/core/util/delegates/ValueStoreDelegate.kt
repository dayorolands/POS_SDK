package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.annotation.Nonnull
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */

class StringStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: String? = null,
) : ReadWriteProperty<Any, String?> {
    override operator fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return pref.getString(key, defValue)
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        pref.edit {
            putString(key, value)
        }
    }
}

fun SharedPreferences.stringStore(key: String, defValue: String? = null): StringStoreDelegate {
    return StringStoreDelegate(this, key, defValue)
}

class LongStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: Long,
) {
    operator fun getValue(obj: Any, prop: KProperty<*>): Long {
        return pref.getLong(key, defValue)
    }

    operator fun setValue(obj: Any, prop: KProperty<*>, newValue: Long) {
        pref.edit {
            putLong(key, newValue)
        }
    }
}

fun SharedPreferences.longStore(key: String, defValue: Long): LongStoreDelegate {
    return LongStoreDelegate(this, key, defValue)
}

class IntStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: Int,
) : ReadWriteProperty<Any, Int> {
    override operator fun getValue(obj: Any, prop: KProperty<*>): Int {
        return pref.getInt(key, defValue)
    }

    override operator fun setValue(obj: Any, prop: KProperty<*>, newValue: Int) {
        pref.edit {
            putInt(key, newValue)
        }
    }
}

fun SharedPreferences.intStore(key: String, defValue: Int): IntStoreDelegate {
    return IntStoreDelegate(this, key, defValue)
}

val defaultJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    useArrayPolymorphism = true
    encodeDefaults = true
}

class JsonStoreDelegate<T : Any>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val serializer: KSerializer<T>,
    private val json: Json,
) {
    operator fun getValue(obj: Any?, prop: KProperty<*>): T? {
        val value = prefs.getString(key, null) ?: return null
        return json.decodeFromString(serializer, value)
    }

    operator fun setValue(obj: Any?, prop: KProperty<*>, newValue: T?) {
        val serializedValue = if (newValue == null) null
        else json.encodeToString(serializer, newValue)

        prefs.edit {
            putString(key, serializedValue)
        }
    }
}

inline fun <reified T : Any> SharedPreferences.jsonStore(
    key: String,
    serializer: KSerializer<T>,
    json: Json = defaultJson,
): JsonStoreDelegate<T> {
    return JsonStoreDelegate(this, key, serializer, json)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.valueStore(key: String): ReadWriteProperty<Any, T?> {
    return when (T::class) {
        String::class -> StringStoreDelegate(this, key, null)
        Int::class -> IntStoreDelegate(this, key, 0)
        else -> throw IllegalArgumentException("${T::class.java.name} is not supported")
    } as ReadWriteProperty<Any, T?>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.valueStore(
    key: String,
    @Nonnull defValue: T,
): ReadWriteProperty<Any, T> {
    return when (T::class) {
        String::class -> StringStoreDelegate(this, key, defValue as String)
        Int::class -> IntStoreDelegate(this, key, defValue as Int)
        else -> throw IllegalArgumentException("type ${T::class.java.name} is not supported in valueStore")
    } as ReadWriteProperty<Any, T>
}