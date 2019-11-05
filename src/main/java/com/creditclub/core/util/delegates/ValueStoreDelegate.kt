package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */

class ValueStoreDelegate<T : Any>(
    private val key: String,
    private val tClass: KClass<T>,
    private val defValue: T? = null
) {
    operator fun getValue(pref: SharedPreferences, prop: KProperty<*>): T? {
        return when (tClass) {
            String::class -> pref.getString(key, defValue as String?) as T?
            Int::class -> pref.getInt(key, 0) as T?
            else -> null
        }
    }

    operator fun setValue(pref: SharedPreferences, prop: KProperty<*>, newValue: T?) {
        pref.edit {
            when (newValue) {
                is String? -> putString(key, newValue as String?)
                is Int -> putInt(key, newValue as Int? ?: 0)
            }
        }
    }
}

inline fun <reified T : Any> valueStore(key: String, defValue: T? = null): ValueStoreDelegate<T> {
    return when (T::class) {
        String::class -> ValueStoreDelegate(key, String::class as KClass<T>, defValue)
        else -> throw IllegalArgumentException("${T::class.java.name} is not supported")
    }
}