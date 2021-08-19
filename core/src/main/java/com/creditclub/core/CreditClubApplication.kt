package com.creditclub.core

import android.app.Application
import com.creditclub.core.data.api.VersionService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.util.safeRunIO
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import org.koin.android.ext.android.inject


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
open class CreditClubApplication : Application() {

    open val otaAppName: String get() = getString(R.string.ota_app_name)
    val appDataStorage: AppDataStorage by inject()

    override fun onCreate() {
        super.onCreate()

        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            return
        }
    }

    open suspend fun getLatestVersion() = safeRunIO {
        val versionService: VersionService by retrofitService()
        val newVersion = versionService.getLatestVersionAndDownloadLink(otaAppName)

        if (newVersion != null) {
            val previousVersion = appDataStorage.latestVersion
            previousVersion?.run {
                if (version == newVersion.version) {
                    newVersion.notifiedAt = previousVersion.notifiedAt
                }
            }
            appDataStorage.latestVersion = newVersion
        }

        newVersion
    }
}