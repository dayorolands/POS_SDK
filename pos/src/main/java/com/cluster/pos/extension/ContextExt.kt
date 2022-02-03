package com.cluster.pos.extension

import android.content.Context
import android.net.ConnectivityManager

inline val Context.apnInfo: String
    get() {
        val mag = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            return mag.activeNetworkInfo?.extraInfo ?: "No active network. Turn on mobile data"
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return "No active network. Turn on mobile data"
    }