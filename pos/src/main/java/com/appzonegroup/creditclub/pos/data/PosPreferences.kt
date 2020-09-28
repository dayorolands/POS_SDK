package com.appzonegroup.creditclub.pos.data

import android.content.Context
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.pos.model.BinRoutes
import kotlinx.serialization.builtins.list

inline val Context.posPreferences get() = PosPreferences(this)

class PosPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("POS_PREFERENCES", 0)
    var binRoutes: List<BinRoutes>? by prefs.jsonStore("BIN_ROUTES", BinRoutes.serializer().list)
    val hasBinRoutes get() = prefs.contains("BIN_ROUTES")
}