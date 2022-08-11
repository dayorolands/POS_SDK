package com.cluster.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.annotation.Nonnull
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
) : ReadWriteProperty<Any, Long> {
    override operator fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return pref.getLong(key, defValue)
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        pref.edit {
            putLong(key, value)
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
    override operator fun getValue(thisRef: Any, property: KProperty<*>): Int {
        return pref.getInt(key, defValue)
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: Int) {
        pref.edit {
            putInt(key, value)
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
    private var _value: T? = null

    operator fun getValue(obj: Any?, prop: KProperty<*>): T? {
        if (_value == null) {
            val value = prefs.getString(key, null) ?: return null
            _value = json.decodeFromString(serializer, value)
        }

        return _value
    }

    operator fun setValue(obj: Any?, prop: KProperty<*>, newValue: T?) {
        val serializedValue = if (newValue == null) null
        else json.encodeToString(serializer, newValue)

        prefs.edit {
            putString(key, serializedValue)
        }
        _value = newValue
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
        Long::class -> LongStoreDelegate(this, key, 0L)
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
        Long::class -> LongStoreDelegate(this, key, defValue as Long)
        else -> throw IllegalArgumentException("type ${T::class.java.name} is not supported in valueStore")
    } as ReadWriteProperty<Any, T>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.addItemToList(
    spListKey: String ,
    item: T
){
    val savedList = getList<T>(spListKey).toMutableList()
    savedList.add(item)
    val listJson = Gson().toJson(savedList)
    edit { putString(spListKey, listJson) }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> SharedPreferences.getList(
    spListKey: String
): List<T>{
    val listJson = getString(spListKey, "")
    if(!listJson.isNullOrBlank()){
        val type = object : TypeToken<List<T>>() {}.type
        return Gson().fromJson(listJson, type)
    }
    return listOf()
}