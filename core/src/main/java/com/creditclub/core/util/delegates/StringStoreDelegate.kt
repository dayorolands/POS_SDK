package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class StringStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: String? = null
) {
    operator fun getValue(obj: Any, prop: KProperty<*>): String? {
        return pref.getString(key, defValue)
    }

    operator fun setValue(obj: Any, prop: KProperty<*>, newValue: String?) {
        pref.edit {
            putString(key, newValue)
        }
    }
}

fun SharedPreferences.stringStore(key: String, defValue: String? = null): StringStoreDelegate {
    return StringStoreDelegate(this, key, defValue)
}