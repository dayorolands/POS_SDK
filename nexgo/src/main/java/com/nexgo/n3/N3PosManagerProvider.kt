package com.nexgo.n3

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider
import com.nexgo.BuildConfig

class N3PosManagerProvider : PosManagerProvider(packageName = BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = N3PosManager
}