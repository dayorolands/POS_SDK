package com.creditclub.core

import android.app.Application
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.jakewharton.threetenabp.AndroidThreeTen
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
}