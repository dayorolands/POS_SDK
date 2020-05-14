package com.appzonegroup.app.fasttrack

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.appzonegroup.app.fasttrack.app.LocalInstitutionConfig
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.ui.MyDialogProvider
import com.appzonegroup.app.fasttrack.utility.extensions.registerWorkers
import com.appzonegroup.app.fasttrack.utility.registerAppFunctions
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.loadPosModules
import com.appzonegroup.creditclub.pos.startPosApp
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.Encryption
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.localStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

class BankOneApplication : CreditClubApplication() {

    override val otaAppName: String
        get() = if (Platform.isPOS) "${super.otaAppName}POS" else super.otaAppName

    override val modules: KoinAppDeclaration?
        get() = {
            modules(module {
                single<IInstitutionConfig> { LocalInstitutionConfig.create(androidContext()) }
                factory<DialogProvider>(override = true) { (context: Context) ->
                    MyDialogProvider(context)
                }
                single<BackendConfig> {
                    object : BackendConfig {
                        override val apiHost = BuildConfig.API_HOST
                        override val posNotificationToken = BuildConfig.NOTIFICATION_TOKEN
                    }
                }
            })

            if (Platform.isPOS) loadPosModules()
        }

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

        registerAppFunctions()
        registerWorkers()

        if (Platform.isPOS) {
            startPosApp()
        }
    }
}
