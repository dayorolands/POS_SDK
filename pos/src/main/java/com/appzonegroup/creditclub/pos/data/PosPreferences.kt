package com.appzonegroup.creditclub.pos.data

import android.content.SharedPreferences
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.pos.model.BinRoutes

class PosPreferences(private val prefs: SharedPreferences) : SharedPreferences by prefs {
    var binRoutes: List<BinRoutes>? by prefs.jsonStore("BIN_ROUTES")
    val hasBinRoutes get() = prefs.contains("BIN_ROUTES")
    fun clearBinRoutes() = prefs.edit().remove("BIN_ROUTES").apply()
}