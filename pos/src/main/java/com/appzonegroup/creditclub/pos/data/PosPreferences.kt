package com.appzonegroup.creditclub.pos.data

import android.content.SharedPreferences
import com.cluster.core.util.delegates.jsonStore
import com.cluster.pos.model.BinRoutes
import kotlinx.serialization.builtins.ListSerializer

class PosPreferences(private val prefs: SharedPreferences) : SharedPreferences by prefs {
    var binRoutes: List<BinRoutes>? by prefs.jsonStore(
        "BIN_ROUTES",
        ListSerializer(BinRoutes.serializer())
    )
    val hasBinRoutes get() = prefs.contains("BIN_ROUTES")
    fun clearBinRoutes() = prefs.edit().remove("BIN_ROUTES").apply()
}