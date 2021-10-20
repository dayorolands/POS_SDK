package com.creditclub.pos.providers.sunmi

import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosManagerProvider

class SunmiPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = SunmiPosManager
}