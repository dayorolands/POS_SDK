package com.dspread.qpos

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider
import com.dspread.BuildConfig

class QPosManagerProvider : PosManagerProvider(
    packageName = BuildConfig.LIBRARY_PACKAGE_NAME,
    registerFirst = false,
) {
    override val posManagerCompanion: PosManagerCompanion
        get() = QPosManager
}