package com.appzonegroup.app.fasttrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.appzonegroup.app.fasttrack.di.*
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.utility.extensions.registerWorkers
import com.appzonegroup.app.fasttrack.utility.registerAppFunctions
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.analytics.AnalyticsObjectBox
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.R
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.prefs.moveTo
import com.creditclub.core.util.localStorage
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BankOneApplication : CreditClubApplication() {

    override val otaAppName: String
        get() = if (Platform.isPOS) "${super.otaAppName}${Platform.posId}" else super.otaAppName

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
        AnalyticsObjectBox.init(this)

        startKoin {
            androidLogger()
            androidContext(this@BankOneApplication)

            modules(
                listOf(
                    apiModule,
                    locationModule,
                    dataModule,
                    uiModule,
                    configModule
                )
            )
        }

        encryptAgentInfo()

        registerAppFunctions()
        Platform.test(this)
        if (Platform.isPOS) startPosApp()
        registerWorkers()
    }

    private fun encryptAgentInfo() {
        val prefsName = getString(R.string.DATA_SOURCE)
        val prefs = getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        )
        if (prefs.contains(LocalStorage.AGENT_INFO)) {
            prefs.moveTo(get<LocalStorage>())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                deleteSharedPreferences(prefsName)
            }
        }
    }
}
