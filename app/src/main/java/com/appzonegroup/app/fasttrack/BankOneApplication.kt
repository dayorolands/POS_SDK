package com.appzonegroup.app.fasttrack

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.appzonegroup.app.fasttrack.app.LocalInstitutionConfig
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.ui.MyDialogProvider
import com.appzonegroup.app.fasttrack.utility.registerAppFunctions
import com.crashlytics.android.Crashlytics
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.localStorage
import io.fabric.sdk.android.Fabric
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

class BankOneApplication : CreditClubApplication() {

    override val modules: KoinAppDeclaration?
        get() = {
            modules(module {
                single { institutionConfig }
                factory<DialogProvider>(override = true) { (context: Context) ->
                    MyDialogProvider(context)
                }
            })
        }

    val authResponse: AuthResponse
        get() {
            if (!localStorage.agentIsActivated) return AuthResponse("","")
            val phoneNumber = "234${localStorage.agent?.phoneNumber?.substring(1)}"

            return AuthResponse(phoneNumber, localStorage.agent?.agentCode)
        }

    private val institutionConfig: IInstitutionConfig
        get() {
            val config = LocalInstitutionConfig
            config.flows.run {
                tokenWithdrawal.customerPin = resources.getBoolean(R.bool.token_withdrawal_customer_pin)

                if (!resources.getBoolean(R.bool.flow_bvn_update)) bvnUpdate = null
                if (!resources.getBoolean(R.bool.flow_customer_pin_change)) customerPinChange = null
                if (!resources.getBoolean(R.bool.flow_wallet_opening)) walletOpening = null
            }

            return config
        }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Fabric.with(this, Crashlytics())
        registerAppFunctions()
    }
}
