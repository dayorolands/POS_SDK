package com.dspread.qpos

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosManagerProvider
import com.creditclub.pos.PosProviders

class QPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = QPosManager
}