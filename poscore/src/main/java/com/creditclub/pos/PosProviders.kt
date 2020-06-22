package com.creditclub.pos

import android.content.Context
import com.appzonegroup.creditclub.pos.card.PosManagerCompanion

object PosProviders {
    val registered = mutableListOf<PosManagerCompanion>()

    val any get() = registered.isNotEmpty()

    fun register(context: Context, posManagerCompanion: PosManagerCompanion) {
        registered.add(posManagerCompanion)
    }
}