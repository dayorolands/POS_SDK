package com.cluster.pos.providers.smartpeak.p1000

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider
import com.cluster.pos.providers.smartpeak.BuildConfig

class SmartPeakPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME, true) {
    override val posManagerCompanion: PosManagerCompanion
        get() = SmartPeakPosManager
}