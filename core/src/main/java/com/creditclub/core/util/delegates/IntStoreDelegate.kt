package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KProperty

class IntStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String,
    private val defValue: Int
) {
    operator fun getValue(obj: Any, prop: KProperty<*>): Int {
        return pref.getInt(key, defValue)
    }

    operator fun setValue(obj: Any, prop: KProperty<*>, newValue: Int) {
        pref.edit {
            putInt(key, newValue)
        }
    }
}

fun SharedPreferences.intStore(key: String, defValue: Int): IntStoreDelegate {
    return IntStoreDelegate(this, key, defValue)
}