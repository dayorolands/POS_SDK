package com.orda.horizonpay

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.IBinder
import android.util.Log
import androidx.core.graphics.drawable.toIcon
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.debug
import com.cluster.core.util.safeRun
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosParameter
import com.cluster.pos.extensions.*
import com.cluster.pos.printer.PosPrinter
import com.horizonpay.smartpossdk.PosAidlDeviceServiceUtil
import com.horizonpay.smartpossdk.aidl.IAidlDevice
import com.horizonpay.smartpossdk.aidl.emv.AidEntity
import com.horizonpay.smartpossdk.aidl.emv.CapkEntity
import com.horizonpay.smartpossdk.aidl.emv.IAidlEmvL2
import com.orda.horizonpay.utils.AidsUtil
import kotlinx.coroutines.*
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
        delay(1000)
        withContext(Dispatchers.Main){
            suspendCoroutine<Unit> { continuation ->
                PosAidlDeviceServiceUtil.connectDeviceService(activity, object : PosAidlDeviceServiceUtil.DeviceServiceListen{
                    override fun onConnected(horizonDevice: IAidlDevice?) {
                        device = horizonDevice
                        device?.getPinpad(false)
                        device?.m1Card
                        device?.m0Ev1Card
                        device?.sysHandler
                        device?.emvL2?.enableTraceLog(true)

                        HorizonDeviceSingleton.setDevicePrinter(device!!)

                        safeRun {
                            injectAid()
                            injectCapk()
                        }

                        device?.asBinder()?.linkToDeath(deathRecipient, 0)
                        isConnected = true
                        continuation.resume(Unit)
                    }

                    override fun error(horizonDevice: Int) {
                        isConnected = false
                    }

                    override fun onDisconnected() {
                        isConnected = false
                        activity.finish()
                    }

                    override fun onUnCompatibleDevice() {

                    }

                })
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

    private fun downloadAid(){
        device?.emvL2?.deleteAllAids()
        val aidEntityList : List<AidEntity> = AidsUtil.getAllAids()
        for (i in aidEntityList.indices){
            val emvAidPara = aidEntityList[i]
            try {
                device?.emvL2?.addAid(emvAidPara)
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun downloadCapks(){
        device?.emvL2?.deleteAllCapks()
        val capkEntityList : List<CapkEntity> = AidsUtil.getAllCapks()
        try {
            device?.emvL2?.addCapks(capkEntityList)
            for(i in capkEntityList.indices){
                val capkParameter = capkEntityList[i]
                device?.emvL2?.addCapk(capkParameter)
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    private inline val String.prependLength get() = "${length / 2}${this}"

    private fun injectAid(){
        device?.emvL2?.deleteAllAids()
        val aidEntityList = mutableListOf<AidEntity>()
        val jsonArray = posParameter.emvAidList ?: return
        val arrayLength = jsonArray.length()
        for(i in 0 until arrayLength){
            val jsonObject = jsonArray.getJSONObject(i)
            val aidEntity = AidEntity.builder().apply {
                AID(jsonObject.aid15)
                selFlag(0)
                appVersion(jsonObject.appVersion18)
                DDOL(jsonObject.ddol20.prependLength)
                tacDefault(jsonObject.defaultTacValue29)
                tacOnline(jsonObject.tacOnline31)
                tacDenial(jsonObject.tacDenial30)
                rdCtlsFloorLimit(0)
                rdCtlsCvmLimit(0)
                rdCtlsTransLimit(10000)
                rdVisaTransLimit(10000)
                onlinePinCap(true)
                maxTargetPer(jsonObject.maxTargetDomestic25.hexByte.toInt())
                targetPer(jsonObject.targetPercentageDomestic27.hexByte.toInt())
                threshold(100)
            }.build()
            aidEntityList.add(aidEntity)
        }
        aidEntityList.addAll(AidsUtil.getAllAids())
        val result = device?.emvL2?.addAids(aidEntityList)
        debug("Add AID result : $result")
    }

    private fun injectCapk(){
        device?.emvL2?.deleteAllCapks()
        val capkEntityList = mutableListOf<CapkEntity>()
        val jsonArray = posParameter.capkList ?: return
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
        }
        capkEntityList.addAll(AidsUtil.getAllCapks())
        val result = device?.emvL2?.addCapks(capkEntityList)
        debug("Add CAPKs result : $result")
    }

    override fun cleanUpEmv() {
        device = null
        cardReader.endWatch()
    }

    companion object : PosManagerCompanion{
        override val id = "HorizonPOS"
        override val deviceType = 5
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

        fun setDevicePrinter(iAidlDevice: IAidlDevice){
            devicePrinter = iAidlDevice
        }

        fun getDevicePrinter() : IAidlDevice?{
            return devicePrinter
        }
    }
}