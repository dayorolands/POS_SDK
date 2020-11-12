package com.appzonegroup.creditclub.pos.data

import android.content.Context
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.pos.model.BinRoutes

inline val Context.posPreferences get() = PosPreferences(this)

class PosPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("POS_PREFERENCES", 0)
    var binRoutes: List<BinRoutes>? by prefs.jsonStore("BIN_ROUTES")
    val hasBinRoutes get() = prefs.contains("BIN_ROUTES")
    fun clearBinRoutes() = prefs.edit().remove("BIN_ROUTES").apply()
}