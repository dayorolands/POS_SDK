package com.creditclub.pos.providers.sunmi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.printer.MockPosPrinter
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.pos.providers.sunmi.emv.EmvUtil
import com.creditclub.pos.providers.sunmi.utils.ThreadPoolUtil
import org.koin.dsl.module
import sunmi.paylib.SunmiPayKernel
import sunmi.paylib.SunmiPayKernel.ConnectCallback


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class SunmiPosManager(private val context: Context) :
    PosManager {
    private val payKernel: SunmiPayKernel by lazy { SunmiPayKernel.getInstance() }
    private var isConnected = false
    private var configMap: Map<String, String> = mapOf()

    override val cardReader: CardReader by lazy { SunmiCardReader() }

    override val sessionData = PosManager.SessionData()

    override suspend fun loadEmv() {
        payKernel.initPaySDK(context, connectCallback)
        configMap = EmvUtil.getConfig("Nigeria")

        ThreadPoolUtil.executeInCachePool {
            EmvUtil.initKey(payKernel)
            EmvUtil.initAidAndRid(payKernel)
            EmvUtil.setTerminalParam(payKernel, configMap)
        }
    }

    override fun cleanUpEmv() {
        payKernel.destroyPaySDK()
    }

    private val connectCallback: ConnectCallback = object : ConnectCallback {
        override fun onConnectPaySDK() {
            Log.d("SunmiPosManager", "onConnectPaySDK")

            safeRun {
                payKernel.mEMVOptV2
                payKernel.mBasicOptV2
                payKernel.mPinPadOptV2
                payKernel.mReadCardOptV2
                payKernel.mSecurityOptV2
                isConnected = true
            }
        }

        override fun onDisconnectPaySDK() {
            Log.e("SunmiPosManager", "onDisconnectPaySDK")
            isConnected = false
        }
    }

    companion object : PosManagerCompanion {
        override val id = "SunmiPOS"
        override val deviceType = 4

        override val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                SunmiPosManager(activity)
            }
            factory<PosPrinter>(override = true) { (context: Context, dialogProvider: DialogProvider) ->
                MockPosPrinter(
                    context,
                    dialogProvider
                )
            }
        }

        override fun isCompatible(context: Context): Boolean {
            val intent = Intent("sunmi.intent.action.PAY_HARDWARE")
            intent.setPackage("com.sunmi.pay.hardware_v3")

            val pkgManager: PackageManager = context.applicationContext.packageManager
            val services: List<ResolveInfo>? = pkgManager.queryIntentServices(intent, 0)

            return services != null && services.isNotEmpty()
        }

        override fun setup(context: Context) {

        }

        fun checkCompatible(context: Context, block: () -> Unit) {
            SunmiPayKernel.getInstance().run {
                initPaySDK(context, object : ConnectCallback {
                    override fun onDisconnectPaySDK() {
                    }

                    override fun onConnectPaySDK() {
                        block()
                        destroyPaySDK()
                    }
                })
            }
        }
    }
}