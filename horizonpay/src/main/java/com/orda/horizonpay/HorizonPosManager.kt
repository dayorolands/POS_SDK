package com.orda.horizonpay

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.IBinder
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosParameter
import com.cluster.pos.extensions.aid15
import com.cluster.pos.extensions.appVersion18
import com.cluster.pos.extensions.ddol20
import com.cluster.pos.extensions.defaultTacValue29
import com.cluster.pos.extensions.exponent38
import com.cluster.pos.extensions.hash39
import com.cluster.pos.extensions.hashAlgorithm36
import com.cluster.pos.extensions.hexByte
import com.cluster.pos.extensions.hexString
import com.cluster.pos.extensions.keyAlgorithm40
import com.cluster.pos.extensions.keyIndex32
import com.cluster.pos.extensions.maxTargetDomestic25
import com.cluster.pos.extensions.modulus37
import com.cluster.pos.extensions.rid35
import com.cluster.pos.extensions.tacDenial30
import com.cluster.pos.extensions.tacOnline31
import com.cluster.pos.extensions.targetPercentageDomestic27
import com.cluster.pos.printer.PosPrinter
import com.horizonpay.smartpossdk.PosAidlDeviceServiceUtil
import com.horizonpay.smartpossdk.aidl.IAidlDevice
import com.horizonpay.smartpossdk.aidl.emv.AidEntity
import com.horizonpay.smartpossdk.aidl.emv.CapkEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HorizonPosManager(
    private val activity: CreditClubActivity
) : PosManager, KoinComponent {
    private val mainScope = MainScope()
    private var device : IAidlDevice? = null
    private val posParameter : PosParameter by inject()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isConnected = false

    override val sessionData = PosManager.SessionData()
    override val cardReader by lazy {
        HorizonCardReader(
            activity = activity,
            device = device!!,
            sessionData = sessionData
        )
    }

    override suspend fun loadEmv() {
        withContext(Dispatchers.Main){
            suspendCoroutine<Unit> { continuation ->
                val horizonDevice = HorizonDeviceSingleton.getDevice()
                device = horizonDevice

                scope.launch {
                    device?.getPinpad(false)
                    device?.emvL2?.enableTraceLog(true)
                    injectAid()
                    injectCapk()
                    device?.asBinder()?.linkToDeath(deathRecipient, 0)
                }
                continuation.resume(Unit)
            }
        }
    }

    private val deathRecipient: IBinder.DeathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            if (device == null) {
                Log.d("HorizonInit", "binderDied device is null")
                return
            }
            device!!.asBinder().unlinkToDeath(this, 0)
            device = null

            //reBind driver Service
            mainScope.launch {
                loadEmv()
            }
        }
    }

    private inline val String.prependLength get() = "${length / 2}${this}"

    private fun injectAid(){
        val jsonArray = posParameter.emvAidList ?: return
        val aidEntityList = mutableListOf<AidEntity>()
        val arrayLength = jsonArray.length()
        for(i in 0 until arrayLength){
            val jsonObject = jsonArray.getJSONObject(i)
            val aidEntity = AidEntity.builder().apply {
                with(jsonObject) {
                    AID(aid15)
                    selFlag(0)
                    appVersion(appVersion18)
                    DDOL(ddol20.prependLength)
                    tacDefault(defaultTacValue29)
                    tacOnline(tacOnline31)
                    tacDenial(tacDenial30)
                    rdCtlsFloorLimit(0L)
                    rdCtlsCvmLimit(15000L)
                    rdCtlsTransLimit(50000L)
                    rdVisaTransLimit(1000L)
                    onlinePinCap(true)
                    maxTargetPer(maxTargetDomestic25.hexByte.toInt())
                    targetPer(targetPercentageDomestic27.hexByte.toInt())
                }
                threshold(100)
            }.build()
            aidEntityList.add(aidEntity)
            val result = device?.emvL2?.addAids(aidEntityList)
            println("CHECK-POINT Add AID result : $result")
        }
    }

    private fun injectCapk(){
        val jsonArray = posParameter.capkList ?: return
        val capkEntityList = mutableListOf<CapkEntity>()
        val arrayLength = jsonArray.length()
        for(i in 0 until arrayLength){
            val jsonObject = jsonArray.getJSONObject(i)
            val capkEntity = CapkEntity.builder().apply {
                RID(jsonObject.rid35)
                capkIndex(jsonObject.keyIndex32.hexByte.toInt())
                arithInd(jsonObject.keyAlgorithm40.hexByte.toInt())
                hashInd(jsonObject.hashAlgorithm36.hexByte.toInt())
                expDate(byteArrayOf(37, 18, 49).hexString)
                modul(jsonObject.modulus37.replace("\n", ""))
                exponent(jsonObject.exponent38)
                checkSum(jsonObject.hash39)
            }.build()
            capkEntityList.add(capkEntity)
            val result = device?.emvL2?.addCapks(capkEntityList)
            println("CHECK-POINT Add CAPKs result : $result")
        }
    }

    override fun cleanUpEmv() {
        device = null
        cardReader.endWatch()
    }

    companion object : PosManagerCompanion{
        override val id = "HorizonPOS"
        override val deviceType = 7
        override val module = module {
            factory<PosManager> { (activity: CreditClubActivity) ->
                HorizonPosManager(activity)
            }

            factory<PosPrinter> { (context: Context, dialogProvider : DialogProvider) ->
                HorizonPrinter(context, dialogProvider)
            }

        }

        override fun isCompatible(context: Context): Boolean {
            val serviceIntent = createExplicitFromImplicitIntent(
                context,
                Intent().setAction("com.horizonpay.smartpossdk.aidlservice.ACTION")
            )
            return serviceIntent != null
        }

        override fun setup(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                PosAidlDeviceServiceUtil.connectDeviceService(context, object : PosAidlDeviceServiceUtil.DeviceServiceListen{
                    override fun onConnected(horizonDevice: IAidlDevice?) {
                        horizonDevice?.let {
                            HorizonDeviceSingleton.setDevice(it)
                        }
                    }

                    override fun error(horizonDevice: Int) {}

                    override fun onDisconnected() {}

                    override fun onUnCompatibleDevice() {}

                })
            }
        }

        private fun createExplicitFromImplicitIntent(
            context: Context,
            implicitIntent: Intent
        ): Intent? {
            val packageManager = context.packageManager
            val resolveInfoList = packageManager.queryIntentServices(implicitIntent, 0)
            return if (resolveInfoList.size > 0) {
                var serviceInfo = findUniformDriverServiceInfo(resolveInfoList)
                if (serviceInfo == null) {
                    serviceInfo = resolveInfoList[0] as ResolveInfo
                }
                val packageName = serviceInfo.serviceInfo.packageName
                val className = serviceInfo.serviceInfo.name
                val component = ComponentName(packageName, className)
                val explicitIntent = Intent(implicitIntent)
                explicitIntent.component = component
                explicitIntent
            } else {
                null
            }
        }

        private fun findUniformDriverServiceInfo(resolveInfoList: List<ResolveInfo>): ResolveInfo? {
            val var1: Iterator<*> = resolveInfoList.iterator()
            var resolveInfo: ResolveInfo?
            do {
                if (!var1.hasNext()) {
                    return null
                }
                resolveInfo = var1.next() as ResolveInfo?
            } while (!isUniformDriverServiceInfo(resolveInfo))
            return resolveInfo
        }

        private fun isUniformDriverServiceInfo(serviceInfo: ResolveInfo?): Boolean {
            return if (serviceInfo?.serviceInfo != null) {
                "com.horizonpay.uniform.driver" == serviceInfo.serviceInfo.packageName
            } else {
                false
            }
        }

    }

}

class HorizonDeviceSingleton{
    companion object {
        private var devicePrinter : IAidlDevice? = null

        fun setDevice(iAidlDevice: IAidlDevice){
            devicePrinter = iAidlDevice
        }

        fun getDevice() : IAidlDevice?{
            return devicePrinter
        }
    }
}