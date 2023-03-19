package com.orda.horizonpay

import com.cluster.pos.BuildConfig
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider

class HorizonPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = HorizonPosManager
}