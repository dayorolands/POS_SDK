package com.cluster.pos

object PosProviders {
    private val registered_ = mutableListOf<PosManagerCompanion>()
    val registered: List<PosManagerCompanion> get() = registered_

    fun registerFirst(posManagerCompanion: PosManagerCompanion) {
        registered_.add(0, posManagerCompanion)
    }

    fun registerLast(posManagerCompanion: PosManagerCompanion) {
        registered_.add(posManagerCompanion)
    }
}