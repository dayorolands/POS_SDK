package com.creditclub.pos.providers.smartpeak.p1000

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.appzonegroup.creditclub.pos.card.*
import com.basewin.database.DataBaseManager
import com.basewin.services.ServiceManager
import com.creditclub.pos.providers.smartpeak.p1000.utils.GlobalData
import org.koin.dsl.module

class SmartPeakPosManager(val context: Context) : PosManager {
    override val cardReader: CardReader by lazy { SmartPeakCardReader() }

    override val sessionData: PosManager.SessionData
        get() = TODO("Not yet implemented")

    override fun loadEmv() {
        TODO("Not yet implemented")
    }

    override fun cleanUpEmv() {
        TODO("Not yet implemented")
    }

    inner class SmartPeakCardReader : CardReader {
        override suspend fun waitForCard(): CardReaderEvent {
            return CardReaderEvent.CHIP
        }

        override suspend fun read(amountStr: String): CardData? {
            return null
        }

        override fun endWatch() {
            TODO("Not yet implemented")
        }

        override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
            TODO("Not yet implemented")
        }
    }

    companion object : PosManagerCompanion {
        override fun setup(context: Context): Boolean {
            ServiceManager.getInstence().init(context.applicationContext)
            DataBaseManager.getInstance().init(context.applicationContext)
            GlobalData.getInstance().init(context.applicationContext)

            return true
        }

        override val module = module(override = true) {
            single<PosManager>(override = true) { (context: Context) ->
                SmartPeakPosManager(context)
            }
        }

        override fun isCompatible(): Boolean {
            return true
        }
    }
}