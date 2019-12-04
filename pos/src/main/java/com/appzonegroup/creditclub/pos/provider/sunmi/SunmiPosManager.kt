package com.appzonegroup.creditclub.pos.provider.sunmi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import com.appzonegroup.creditclub.pos.PosActivity
import com.appzonegroup.creditclub.pos.card.CardReader
import com.appzonegroup.creditclub.pos.card.PosManager
import com.appzonegroup.creditclub.pos.provider.sunmi.emv.EmvUtil
import com.appzonegroup.creditclub.pos.provider.sunmi.utils.ThreadPoolUtil
import com.creditclub.core.util.safeRun
import org.koin.dsl.module
import sunmi.paylib.SunmiPayKernel
import sunmi.paylib.SunmiPayKernel.ConnectCallback


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class SunmiPosManager(val context: Context) : PosManager {
    private val payKernel: SunmiPayKernel by lazy { SunmiPayKernel.getInstance() }
    private var isConnected = false
    private var configMap: Map<String, String> = mapOf()

    override val cardReader: CardReader
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val sessionData: PosManager.SessionData
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun loadEmv() {
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

    companion object {
        val module = module {
            factory<PosManager>(override = true) { (activity: PosActivity) ->
                SunmiPosManager(activity)
            }
        }

        fun isCompatible(context: Context): Boolean {
            val intent = Intent("sunmi.intent.action.PAY_HARDWARE")
            intent.setPackage("com.sunmi.pay.hardware_v3")

            val pkgManager: PackageManager = context.applicationContext.packageManager
            val services: List<ResolveInfo>? = pkgManager.queryIntentServices(intent, 0)

            return services != null && services.isNotEmpty()
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