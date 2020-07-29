package com.creditclub.pos.utils

import android.content.SharedPreferences
import kotlin.reflect.KProperty

class NonNullStringStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: String = ""
) {
    operator fun getValue(obj: Any, prop: KProperty<*>): String {
        return pref.getString(key, defValue) ?: defValue
    }

    operator fun setValue(obj: Any, prop: KProperty<*>, newValue: String) {
        pref.edit().putString(key, newValue).apply()
    }
}

fun SharedPreferences.nonNullStringStore(
    key: String,
    defValue: String = ""
): NonNullStringStoreDelegate {
    return NonNullStringStoreDelegate(this, key, defValue)
}