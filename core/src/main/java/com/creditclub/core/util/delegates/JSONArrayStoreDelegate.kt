package com.creditclub.core.util.delegates

import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import kotlin.reflect.KProperty

class JSONArrayStoreDelegate(
    private val pref: SharedPreferences,
    private val key: String
) {
    operator fun getValue(obj: Any, prop: KProperty<*>): JSONArray? {
        val value = pref.getString(key, null) ?: return null
        return JSONArray(value)
    }

    operator fun setValue(obj: Any, prop: KProperty<*>, newValue: JSONArray?) {
        pref.edit {
            putString(key, newValue?.toString())
        }
    }
}

fun SharedPreferences.jsonArrayStore(key: String): JSONArrayStoreDelegate {
    return JSONArrayStoreDelegate(this, key)
}