package com.creditclub.pos.nexgo.nexgo_n86

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider
import org.koin.android.BuildConfig

class N86PosManagerProvider : PosManagerProvider(packageName = BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = N86PosManager
}