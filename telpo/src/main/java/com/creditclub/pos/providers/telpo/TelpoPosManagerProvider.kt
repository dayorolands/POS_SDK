package com.creditclub.pos.providers.telpo

import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosManagerProvider

class TelpoPosManagerProvider : PosManagerProvider(packageName = BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = TelpoPosManager
}