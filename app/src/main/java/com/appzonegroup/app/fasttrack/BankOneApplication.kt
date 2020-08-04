package com.appzonegroup.app.fasttrack

import androidx.appcompat.app.AppCompatDelegate
import com.appzonegroup.app.fasttrack.di.*
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.utility.extensions.registerWorkers
import com.appzonegroup.app.fasttrack.utility.registerAppFunctions
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.util.localStorage
import com.squareup.picasso.Picasso
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BankOneApplication : CreditClubApplication() {

    override val otaAppName: String
        get() = if (Platform.isPOS) "${super.otaAppName}POS" else super.otaAppName

    val authResponse: AuthResponse by lazy {
        val phoneNumber = "234${localStorage.agentPhone?.substring(1)}"
        val newAuth = localStorage.authResponse
            ?: return@lazy AuthResponse(
                phoneNumber,
                localStorage.getString("AGENT_CODE")
            )

        return@lazy AuthResponse(
            newAuth.phoneNumber ?: phoneNumber,
            newAuth.activationCode ?: localStorage.getString("AGENT_CODE")
        )
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Picasso.setSingletonInstance(
            Picasso.Builder(this).loggingEnabled(BuildConfig.DEBUG).build()
        )

        startKoin {
            androidLogger()
            androidContext(this@BankOneApplication)

            modules(listOf(
                apiModule,
                locationModule,
                dataModule,
                uiModule,
                configModule
            ))

            if (Platform.isPOS) loadPosModules()
        }

        registerAppFunctions()
        registerWorkers()

        if (Platform.isPOS) {
            startPosApp()
        }
    }
}
