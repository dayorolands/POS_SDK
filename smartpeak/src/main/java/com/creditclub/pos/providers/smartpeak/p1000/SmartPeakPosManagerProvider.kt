package com.creditclub.pos.providers.smartpeak.p1000

import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosManagerProvider
import com.creditclub.pos.providers.smartpeak.BuildConfig

class SmartPeakPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = SmartPeakPosManager
}