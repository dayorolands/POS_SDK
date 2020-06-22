package com.creditclub.pos.providers.smartpeak.p1000

import android.content.Context
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

        override fun waitForCard(onEventChange: CardReaderEventListener) {
            TODO("Not yet implemented")
        }

        override fun read(amountStr: String, onReadCard: CardDataListener) {
            TODO("Not yet implemented")
        }

        override fun endWatch() {
            TODO("Not yet implemented")
        }

        override suspend fun startWatch(onEventChange: CardReaderEventListener) {
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
            return false
        }
    }
}