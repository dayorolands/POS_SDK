package com.cluster.pos.providers.sunmi

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.debug
import com.cluster.core.util.safeRun
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardReader
import com.cluster.pos.extensions.*
import com.cluster.pos.printer.MockPosPrinter
import com.cluster.pos.printer.PosPrinter
import com.cluster.pos.providers.sunmi.emv.EmvUtil
import com.sunmi.pay.hardware.aidlv2.bean.AidV2
import com.sunmi.pay.hardware.aidlv2.bean.CapkV2
import com.sunmi.pay.hardware.aidlv2.bean.EmvTermParamV2
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import sunmi.paylib.SunmiPayKernel
import sunmi.paylib.SunmiPayKernel.ConnectCallback
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class SunmiPosManager(private val activity: CreditClubActivity) : PosManager, KoinComponent {
    private val payKernel: SunmiPayKernel = SunmiPayKernel.getInstance()
    private var isConnected = false
    private val posParameter: PosParameter by inject()
    override val cardReader: CardReader by lazy {
        SunmiCardReader(
            activity,
            payKernel,
            sessionData
        )
    }

    override val sessionData = PosManager.SessionData()

    override suspend fun loadEmv() {
        withContext(Dispatchers.IO) {
            suspendCoroutine<Unit> { continuation ->
                payKernel.initPaySDK(activity, object : ConnectCallback {
                    override fun onConnectPaySDK() {
                        Log.d("SunmiPosManager", "onConnectPaySDK")

                        val emvOptV2 = payKernel.mEMVOptV2
                        payKernel.mBasicOptV2
                        payKernel.mPinPadOptV2
                        payKernel.mReadCardOptV2
                        payKernel.mSecurityOptV2
                        isConnected = true

                        EmvUtil.initKey(payKernel)
                        EmvUtil.initAidAndRid(payKernel)

                        emvOptV2.injectAid()
                        emvOptV2.injectCapk()

                        val emvTermParam = EmvTermParamV2().apply {
                            countryCode = "0566"
                            capability = "E040C8"
                        }
                        val result: Int = emvOptV2.setTerminalParam(emvTermParam)
                        debug("Sunmi setTerminalParam result is $result")
                        continuation.resume(Unit)
                    }

                    override fun onDisconnectPaySDK() {
                        Log.e("SunmiPosManager", "onDisconnectPaySDK")
                        isConnected = false
                        activity.finish()
                    }
                })
            }
        }
    }

    private fun EMVOptV2.injectAid() {
        val jsonArray = posParameter.emvAidList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength) {
            val jsonObject = jsonArray.getJSONObject(i)
            val emvApp = AidV2()
//            emvApp.AppName = jsonObject.appName17.toByteArray(StandardCharsets.US_ASCII)
            emvApp.aid = jsonObject.aid15.hexBytes
            emvApp.selFlag = 0
//            emvApp.Priority = jsonObject.selectionPriority19.hexByte
            emvApp.targetPer = jsonObject.targetPercentageDomestic27.hexByte
            emvApp.maxTargetPer = jsonObject.maxTargetDomestic25.hexByte
            emvApp.floorLimit = byteArrayOf(1)
            emvApp.randTransSel = 1
            emvApp.velocityCheck = 1
            emvApp.floorLimit = jsonObject.tflDomestic22.hexBytes
            emvApp.threshold = jsonObject.offlineThresholdDomestic24.hexBytes
            emvApp.TACDenial = jsonObject.tacDenial30.hexBytes
            emvApp.TACOnline = jsonObject.tacOnline31.hexBytes
            emvApp.TACDefault = jsonObject.defaultTacValue29.hexBytes
            emvApp.AcquierId = byteArrayOf(1, 35, 69, 103, -119, 16)
            emvApp.dDOL = jsonObject.ddol20.prependLength.hexBytes
            emvApp.tDOL = jsonObject.tdol21.prependLength.hexBytes
            emvApp.version = jsonObject.appVersion18.hexBytes
            emvApp.riskManData =
                byteArrayOf(0x6C, 0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            val ret = addAid(emvApp)
            if (ret < 0) debug("Error setting AID value ${emvApp.aid}")
        }
    }

    private inline val String.prependLength get() = "${length / 2}${this}"

    private fun EMVOptV2.injectCapk() {
        val jsonArray = posParameter.capkList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength) {
            val jsonObject = jsonArray.getJSONObject(i)
            val capk = CapkV2()
            capk.rid = jsonObject.rid35.hexBytes
            capk.index = jsonObject.keyIndex32.hexByte
            capk.hashInd = jsonObject.hashAlgorithm36.hexByte
            capk.arithInd = jsonObject.keyAlgorithm40.hexByte
            capk.modul = jsonObject.modulus37.replace("\n", "").hexBytes
            capk.exponent = jsonObject.exponent38.hexBytes
            capk.expDate = "20311222".hexBytes
//            capk.checkSum = jsonObject.hash39.hexBytes
            val ret = addCapk(capk)
            if (ret < 0) debug("Error setting CAPK value ${capk.rid}")
        }
    }

    override fun cleanUpEmv() {
        payKernel.destroyPaySDK()
    }

    companion object : PosManagerCompanion {
        override val id = "SunmiPOS"
        override val deviceType = 4

        override val module = module {
            factory<PosManager> { (activity: CreditClubActivity) ->
                SunmiPosManager(activity)
            }
            factory<PosPrinter> { (context: Context, dialogProvider: DialogProvider) ->
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
            val services: List<ResolveInfo>? = safeRun {
                pkgManager.queryIntentServices(intent, 0)
            }.data

            return services != null && services.isNotEmpty()
        }

        override fun setup(context: Context) {

        }
    }
}
