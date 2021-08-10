package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KProperty

class LongStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: Long
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