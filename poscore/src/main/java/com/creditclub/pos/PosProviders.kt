package com.creditclub.pos

import android.content.Context

object PosProviders {
    private val registered_ = mutableListOf<PosManagerCompanion>()
    val registered: List<PosManagerCompanion> get() = registered_

    fun registerFirst(context: Context, posManagerCompanion: PosManagerCompanion) {
        registered_.add(0, posManagerCompanion)
    }
}