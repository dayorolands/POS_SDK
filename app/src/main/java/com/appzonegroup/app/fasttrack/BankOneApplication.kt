package com.appzonegroup.app.fasttrack

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.appzonegroup.app.fasttrack.app.LocalInstitutionConfig
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.ui.MyDialogProvider
import com.appzonegroup.app.fasttrack.utility.registerAppFunctions
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.loadPosModules
import com.appzonegroup.creditclub.pos.startPosApp
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.localStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

class BankOneApplication : CreditClubApplication() {

    override val modules: KoinAppDeclaration?
        get() = {
            modules(module {
                single<IInstitutionConfig> { LocalInstitutionConfig.create(androidContext()) }
                factory<DialogProvider>(override = true) { (context: Context) ->
                    MyDialogProvider(context)
                }
            })

            if (Platform.isPOS) loadPosModules()
        }

    val authResponse: AuthResponse
        get() {
            val defaults: AuthResponse.() -> Unit = { sessionId = sessionId ?: "nothing" }

            if (!localStorage.agentIsActivated) return AuthResponse("", "").apply(defaults)
            val phoneNumber = "234${localStorage.agent?.phoneNumber?.substring(1)}"

            return AuthResponse(phoneNumber, localStorage.agent?.agentCode).apply(defaults)
        }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        registerAppFunctions()

        if (Platform.isPOS) {
            startPosApp()
        }
    }
}
