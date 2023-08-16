package com.nexgo.n86

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider

class N86PosManagerProvider : PosManagerProvider(packageName = "com.nexgo") {
    override val posManagerCompanion: PosManagerCompanion
        get() = N86PosManager
}