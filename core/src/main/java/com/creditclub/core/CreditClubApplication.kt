package com.creditclub.core

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.util.appDataStorage
import com.creditclub.core.util.safeRunIO
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import org.koin.android.ext.android.get


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
open class CreditClubApplication : Application() {

    open val otaAppName: String get() = getString(R.string.ota_app_name)

    override fun onCreate() {
        super.onCreate()

        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            return
        }

        val myConfig = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.INFO)
            .build()

        WorkManager.initialize(this, myConfig)
    }

    open suspend fun getLatestVersion() = safeRunIO {
        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI = get()
        val newVersion =
            creditClubMiddleWareAPI.versionService.getLatestVersionAndDownloadLink(otaAppName)

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