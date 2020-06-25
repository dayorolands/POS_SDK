package com.creditclub.pos

import android.content.Context
import com.appzonegroup.creditclub.pos.PosManagerCompanion

object PosProviders {
    val registered = mutableListOf<PosManagerCompanion>()

    val any get() = registered.isNotEmpty()

    fun registerFirst(context: Context, posManagerCompanion: PosManagerCompanion) {
        registered.add(0, posManagerCompanion)
    }

    fun registerLast(context: Context, posManagerCompanion: PosManagerCompanion) {
        registered.add(posManagerCompanion)
    }
}