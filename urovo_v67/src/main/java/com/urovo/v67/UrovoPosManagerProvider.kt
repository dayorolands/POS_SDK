package com.urovo.v67

import com.cluster.pos.BuildConfig
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider

class UrovoPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = UrovoPosManager
}