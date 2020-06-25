package com.appzonegroup.creditclub.pos.provider.telpo

import android.content.Context
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.PosManager
import com.appzonegroup.creditclub.pos.PosManagerCompanion
import com.appzonegroup.creditclub.pos.printer.PosPrinter
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.safeRun
import com.telpo.emv.EmvService
import com.telpo.pinpad.PinpadService
import com.telpo.tps550.api.util.SystemUtil
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class TelpoPosManager(val activity: CreditClubActivity) : PosManager, KoinComponent {
    override val cardReader by lazy { TelpoCardReader(activity, emvListener) }

    private val emvListener by lazy {
        TelpoEmvListener(activity, emvService, sessionData)
    }

    private val emvService by lazy { EmvService.getInstance() }
    override val sessionData = PosManager.SessionData()

    override suspend fun loadEmv() {
        safeRun { PinpadService.Close() }

        emvService.setListener(emvListener)

        EmvService.Emv_SetDebugOn(if (BuildConfig.DEBUG) 1 else 0)

        StartEmvService(activity).run()
        StartPinPadService(activity, get()).run()

        EmvService.Emv_RemoveAllApp()
        EmvService.Emv_RemoveAllCapk()

        StableAPPCAPK.Add_All_APP()
        StableAPPCAPK.Add_All_CAPK()
    }

    override fun cleanUpEmv() {
        PinpadService.Close()
        EmvService.deviceClose()
    }

    companion object : PosManagerCompanion {
        var deviceType = -1
            private set

        override val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                TelpoPosManager(activity)
            }
            factory<PosPrinter>(override = true) { (context: Context, dialogProvider: DialogProvider) ->
                TelpoPrinter(context, dialogProvider)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            try {
                deviceType = SystemUtil.getDeviceType()
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) ex.printStackTrace()
            } catch (err: UnsatisfiedLinkError) {
                if (BuildConfig.DEBUG) err.printStackTrace()
            }

            return false
        }

        override fun setup(context: Context) {}
    }
}