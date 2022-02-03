@file:JvmName("PrefsExt")

package com.cluster.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun Context.getEncryptedSharedPreferences(fileName: String): SharedPreferences {
    val masterKeyAlias = MasterKey.Builder(this)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    return EncryptedSharedPreferences.create(
        applicationContext,
        fileName,
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

fun SharedPreferences.moveTo(newPrefs: SharedPreferences) {
    val oldPrefs = this
    newPrefs.edit {
        for ((key, value) in oldPrefs.all) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
            }
        }
    }

    edit {
        clear()
    }
}