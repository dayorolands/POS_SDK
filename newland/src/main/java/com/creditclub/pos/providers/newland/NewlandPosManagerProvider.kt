package com.creditclub.pos.providers.newland

import com.cluster.pos.BuildConfig
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider

class NewlandPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = NewlandPosManager
}
