package com.nexgo.n3

import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosManagerProvider
import com.nexgo.BuildConfig

class N3PosManagerProvider : PosManagerProvider(packageName = BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = N3PosManager
}