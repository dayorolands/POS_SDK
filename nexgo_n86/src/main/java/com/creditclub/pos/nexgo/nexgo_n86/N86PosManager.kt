package com.creditclub.pos.nexgo.nexgo_n86

import android.content.Context
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.debug
import com.cluster.core.util.debugOnly
import com.cluster.pos.PosConfig
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardReaders
import com.cluster.pos.card.TransactionType
import com.cluster.pos.extensions.*
import com.creditclub.pos.providers.newland.util.getDefaultAidList
import com.creditclub.pos.providers.newland.util.getDefaultCapkList
import com.nexgo.common.LogUtils
import com.nexgo.oaf.apiv3.APIProxy
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.emv.AidEntity
import com.nexgo.oaf.apiv3.emv.CapkEntity
import org.koin.dsl.module

class N86PosManager(
    private val activity : CreditClubActivity,
    private val posParameter: PosParameter,
    private val posConfig: PosConfig
) : PosManager{
    private val deviceEngine : DeviceEngine = APIProxy.getDeviceEngine(activity)
    private val emvHandler2 = deviceEngine.getEmvHandler("app2")
    override val sessionData = PosManager.SessionData()
    override val cardReader: CardReaders = N86CardReader(
        activity = activity,
        deviceEngine = deviceEngine,
        sessionData = sessionData,
        posManager = this,
        defaultPosParameter = posParameter,
        posConfig = posConfig,
    )

    override suspend fun loadEmv() {
        Log.d("CardInfoEntity", "Model is ${APIProxy.getDeviceEngine(activity).deviceInfo.model}")
        debugOnly {
            emvHandler2.emvDebugLog(true)
            LogUtils.setDebugEnable(true)
        }
        sessionData.reset()
        injectAid()
        injectCapk()
    }

    override fun cleanUpEmv() {
        sessionData.reset()
    }

    private fun injectAid() {
        emvHandler2.delAllAid()
        val aidEntityList = mutableListOf<AidEntity>()
        val jsonArray = posParameter.emvAidList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength) {
            val jsonObject = jsonArray.getJSONObject(i)
            val aidEntity = AidEntity().apply {
                aid = jsonObject.aid15
                targetPercent = jsonObject.targetPercentageDomestic27.hexByte.toInt()
                maxTargetPercent = jsonObject.maxTargetDomestic25.hexByte.toInt()
                tacDenial = jsonObject.tacDenial30
                tacOnline = jsonObject.tacOnline31
                tacDefault = jsonObject.defaultTacValue29
                setDdol(jsonObject.ddol20.prependLength)
                appVerNum = jsonObject.appVersion18
                onlinePinCap = 1
            }
            aidEntityList.add(aidEntity)
        }
        aidEntityList.addAll(getDefaultAidList(activity))
        val result = emvHandler2.setAidParaList(aidEntityList)
        debug("AID Injection result is $result")
    }

    private inline val String.prependLength get() = "${length / 2}${this}"

    private fun injectCapk() {
        emvHandler2.delAllCapk()
        val capkEntityList = mutableListOf<CapkEntity>()
        val jsonArray = posParameter.capkList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength) {
            val jsonObject = jsonArray.getJSONObject(i)
            val capkEntity = CapkEntity().apply {
                rid = jsonObject.rid35
                capkIdx = jsonObject.keyIndex32.hexByte.toInt()
                hashInd = jsonObject.hashAlgorithm36.hexByte.toInt()
                arithInd = jsonObject.keyAlgorithm40.hexByte.toInt()
                modulus = jsonObject.modulus37.replace("\n", "")
                exponent = jsonObject.exponent38
                expireDate = byteArrayOf(37, 18, 49).hexString
            }
            capkEntityList.add(capkEntity)
        }
        capkEntityList.addAll(getDefaultCapkList(activity))
        val result = emvHandler2.setCAPKList(capkEntityList)
        debug("CAPK Injection result is $result")
    }

    private fun PosManager.SessionData.reset() {
        amount = 0L
        canRunTransaction = true
        canManageParameters = false
        transactionType = TransactionType.Unknown
    }

    companion object : PosManagerCompanion {
        override val id = "NexgoN86POS"
        override val deviceType = 9

        override fun setup(context: Context) {
            APIProxy.getDeviceEngine(context).getEmvHandler2("app2")
        }

        override val module = module {
            factory<PosManager> { (activity: CreditClubActivity) ->
                N86PosManager(
                    activity = activity,
                    posParameter = get(),
                    posConfig = get(),
                )
            }
        }

        override fun isCompatible(context: Context): Boolean {
            return APIProxy.getDeviceEngine(context).deviceInfo.model.equals("N86", ignoreCase = true)
        }
    }
}