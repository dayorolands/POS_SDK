package com.cluster.pos.providers.smartpeak.p1000

import android.content.Context
import android.content.Intent
import com.basewin.database.DataBaseManager
import com.basewin.define.InputPBOCInitData
import com.basewin.define.KeyType
import com.basewin.define.PBOCOption
import com.basewin.log.LogUtil
import com.basewin.services.ServiceManager
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.*
import com.cluster.pos.card.CardData
import com.cluster.pos.card.CardReader
import com.cluster.pos.card.CardReaderEvent
import com.cluster.pos.card.CardReaderEventListener
import com.cluster.pos.printer.PosPrinter
import com.cluster.pos.providers.smartpeak.p1000.pboc.utils.OfflineTransactionListener
import com.cluster.pos.providers.smartpeak.p1000.utils.GlobalData
import com.pos.sdk.card.PosCardInfo
import com.pos.sdk.printer.PosPrinterInfo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module

class SmartPeakPosManager(val activity: CreditClubActivity) : PosManager, KoinComponent {
    override val cardReader: CardReader by lazy { SmartPeakCardReader() }
    override val sessionData by lazy { PosManager.SessionData() }
    private val posConfig: PosConfig by inject()
    private val posParameter: PosParameter by inject()

    private val type = KeyType.PIN_KEY
    private val area = 1
    private val tmkindex = 1

    override suspend fun loadEmv() {
        val pinpad = ServiceManager.getInstence().pinpad
        pinpad.loadMainKeyByArea(area, tmkindex, posParameter.masterKey)
        pinpad.loadPinKeyByArea(area, tmkindex, posParameter.pinKey, null)

        val globalData = GlobalData.getInstance()
        globalData.tmkId = tmkindex
        globalData.area = area
    }

    override fun cleanUpEmv() {

    }

    inner class SmartPeakCardReader : CardReader {
        override suspend fun waitForCard(): CardReaderEvent {
            return CardReaderEvent.CHIP
        }

        override suspend fun read(amountStr: String): CardData? {
            try {
                val intent = Intent()
                intent.putExtra(InputPBOCInitData.AMOUNT_FLAG, sessionData.amount)
                intent.putExtra(
                    InputPBOCInitData.USE_DEVICE_FLAG,
                    InputPBOCInitData.USE_MAG_CARD or InputPBOCInitData.USE_RF_CARD or InputPBOCInitData.USE_IC_CARD
                )
                intent.putExtra(InputPBOCInitData.IS_SUPPERT_EC_FLAG, true)
                ServiceManager.getInstence().pboc.startTransfer(
                    PBOCOption.ONLINE_PAY,
                    intent,
                    OfflineTransactionListener(
                        activity,
                        activity.dialogProvider,
                        sessionData
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun endWatch() {

        }

        override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {

        }
    }

    companion object : PosManagerCompanion {
        override val id = "SmartPeakPOS"
        override val deviceType = 6

        override fun setup(context: Context) {
            ServiceManager.getInstence().init(context.applicationContext)
            DataBaseManager.getInstance().init(context.applicationContext)
            GlobalData.getInstance().init(context.applicationContext)
            LogUtil.openLog()
        }

        override val module = module {
            single<PosManager> { (activity: CreditClubActivity) ->
                SmartPeakPosManager(activity)
            }
            single<PosPrinter> { (context: Context, dialogProvider: DialogProvider) ->
                SmartPeakPrinter(context, dialogProvider)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            return try {
                PosCardInfo()
                PosPrinterInfo()
                true
            } catch (ignored: NoClassDefFoundError) {
                false
            }
        }
    }
}