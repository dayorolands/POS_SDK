package com.dspread.qpos

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.view.WindowManager
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.printer.PosPrinter
import com.dspread.xpos.QPOSService
import kotlinx.coroutines.*
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class QPosManager(
    activity: CreditClubActivity,
    defaultPosParameter: PosParameter,
) : PosManager, DialogProvider by activity.dialogProvider {
    private val mainScope = MainScope()
    internal val pos = QPOSService.getInstance(QPOSService.CommunicationMode.BLUETOOTH)
    private val listener = QPosListener(
        pos = pos,
        qPosManager = this,
        defaultPosParameter = defaultPosParameter,
    )

    override val sessionData = PosManager.SessionData()
    override val cardReader: CardReader = QPosCardReader(
        activity = activity,
        pos = pos,
        listener = listener,
        mainScope = mainScope,
        sessionData = sessionData,
    )

    override suspend fun loadEmv() {
        pos.setContext(activity)
        //init handler
    }

    override fun cleanUpEmv() {
        cardReader.endWatch()
        pos.stopScanQPos2Mode()
        listener.cleanup()
        if (pos == null) return
        pos.disconnectBT()
        mainScope.cancel()
    }

    init {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    companion object : PosManagerCompanion {
        override val id = ""
        override val deviceType = 2
        override val module = module {
            factory<PosManager> { (activity: CreditClubActivity) ->
                QPosManager(activity = activity, defaultPosParameter = get())
            }
            factory<PosPrinter> { (activity: CreditClubActivity) ->
                QposPrinter(activity)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            return try {
                BluetoothAdapter.getDefaultAdapter() != null
            } catch (ex: Exception) {
                false
            }
        }

        override fun setup(context: Context) {

        }
    }
}