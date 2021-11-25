package com.dspread.qpos

import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosManagerProvider
import com.dspread.BuildConfig

class QPosManagerProvider : PosManagerProvider(
    packageName = BuildConfig.LIBRARY_PACKAGE_NAME,
    registerFirst = false,
) {
    override val posManagerCompanion: PosManagerCompanion
        get() = QPosManager
}