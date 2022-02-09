package com.cluster.pos.providers.sunmi

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider

class SunmiPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = SunmiPosManager
}