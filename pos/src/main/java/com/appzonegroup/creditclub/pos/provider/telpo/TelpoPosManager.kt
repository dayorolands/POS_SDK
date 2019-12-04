package com.appzonegroup.creditclub.pos.provider.telpo

import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.PosActivity
import com.appzonegroup.creditclub.pos.card.PosManager
import com.creditclub.core.util.safeRun
import com.telpo.emv.EmvService
import com.telpo.pinpad.PinpadService
import com.telpo.tps550.api.util.SystemUtil
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class TelpoPosManager(val activity: PosActivity) : PosManager {
    override val cardReader by lazy { TelpoCardReader(activity, emvListener) }

    private val emvListener by lazy {
        TelpoEmvListener(activity, emvService, sessionData)
    }

    private val emvService by lazy { EmvService.getInstance() }
    override val sessionData = PosManager.SessionData()

    override fun loadEmv() {
        safeRun { PinpadService.Close() }

        emvService.setListener(emvListener)

        EmvService.Emv_SetDebugOn(if (BuildConfig.DEBUG) 1 else 0)

        StartEmvService(activity).run()
        StartPinPadService(activity, activity.parameters).run()

        EmvService.Emv_RemoveAllApp()
        EmvService.Emv_RemoveAllCapk()

        StableAPPCAPK.Add_All_APP()
        StableAPPCAPK.Add_All_CAPK()
    }

    override fun cleanUpEmv() {
        PinpadService.Close()
        EmvService.deviceClose()
    }

    companion object {
        var deviceType = -1
            private set

        val module = module {
            factory<PosManager>(override = true) { (activity: PosActivity) ->
                TelpoPosManager(activity)
            }
        }

        fun isCompatible(): Boolean {
            try {
                deviceType = SystemUtil.getDeviceType()
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) ex.printStackTrace()
            } catch (err: UnsatisfiedLinkError) {
                if (BuildConfig.DEBUG) err.printStackTrace()
            }

            return false
        }
    }
}