package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KProperty

class BooleanStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: Boolean
) {
    operator fun getValue(obj: Any, prop: KProperty<*>): Boolean {
        return pref.getBoolean(key, defValue)
    }

    operator fun setValue(obj: Any, prop: KProperty<*>, newValue: Boolean) {
        pref.edit {
            putBoolean(key, newValue)
        }
    }
}

fun SharedPreferences.booleanStore(key: String, defValue: Boolean): BooleanStoreDelegate {
    return BooleanStoreDelegate(this, key, defValue)
}