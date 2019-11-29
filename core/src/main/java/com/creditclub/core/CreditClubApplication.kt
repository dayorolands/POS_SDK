package com.creditclub.core

import android.app.Application
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.util.appDataStorage
import com.creditclub.core.util.safeRunIO
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.jakewharton.threetenabp.AndroidThreeTen
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
open class CreditClubApplication : Application() {

    private lateinit var koinApp: KoinApplication
    protected open val modules: KoinAppDeclaration? = null
    open val otaAppName: String get() = getString(R.string.ota_app_name)

    override fun onCreate() {
        super.onCreate()

        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            return
        }

        AndroidThreeTen.init(this)

        koinApp = startKoin {
            androidLogger()
            androidContext(this@CreditClubApplication)

            modules(listOf(apiModule, locationModule, dataModule))
            modules?.invoke(this)
        }
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