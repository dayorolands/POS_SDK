package com.urovo.v67

import android.content.Context
import android.os.Build
import com.cluster.core.ui.CreditClubActivity
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.card.CardReaders
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module
import org.koin.dsl.module

class UrovoPosManager(
    private val activity: CreditClubActivity,
) : PosManager, KoinComponent{
    private val mKernelApi = EmvNfcKernelApi.getInstance()
    override val sessionData = PosManager.SessionData()
    override val cardReader: CardReaders by lazy {
        UrovoCardReader(
            activity = activity,
            sessionData = sessionData,
            emvNfcKernelApi = mKernelApi
        )
    }

    override suspend fun loadEmv() {

    }

    override fun cleanUpEmv() {

    }


    companion object : PosManagerCompanion {
        override val id: String = "UrovoPOS"
        override val deviceType: Int = 9
        override val module: Module = module {
            factory<PosManager>{ (activity : CreditClubActivity) ->
                UrovoPosManager(activity)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            val manufacturerName = Build.MANUFACTURER
            return manufacturerName.contains("urovo")
        }

        override fun setup(context: Context) {
        }

    }
}