package com.nexgo.n3

import android.app.Activity
import android.content.Context
import android.util.Log
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.debugOnly
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.TransactionType
import com.creditclub.pos.extensions.*
import com.creditclub.pos.printer.PosPrinter
import com.nexgo.BuildConfig
import com.nexgo.oaf.apiv3.APIProxy
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.emv.AidEntity
import com.nexgo.oaf.apiv3.emv.CapkEntity
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.module

class N3PosManager(private val activity: CreditClubActivity) : PosManager, KoinComponent {
    private val deviceEngine: DeviceEngine = APIProxy.getDeviceEngine(activity)
    private val emvHandler2 = deviceEngine.getEmvHandler2("app2")
    override val sessionData = PosManager.SessionData()
    override val cardReader = N3CardReader(activity, deviceEngine, sessionData, this)
    private val posParameter: PosParameter by inject()

    override suspend fun loadEmv() {
        emvHandler2.emvDebugLog(BuildConfig.DEBUG)
        sessionData.reset()
        injectAid()
        injectCapk()
    }

    private fun injectAid() {
        emvHandler2.delAllAid()
        val aidEntityList = mutableListOf<AidEntity>()
        val jsonArray = posParameter.emvAidList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength) {
            val jsonObject = jsonArray.getJSONObject(i)
            val aidEntity = AidEntity().apply {
//                AppName = jsonObject.appName17.toByteArray(StandardCharsets.US_ASCII)
                aid = jsonObject.aid15
//                SelFlag = 0
//                Priority = jsonObject.selectionPriority19.hexByte
                targetPercent = jsonObject.targetPercentageDomestic27.hexByte.toInt()
                maxTargetPercent = jsonObject.maxTargetDomestic25.hexByte.toInt()
//                FloorLimitCheck = 1
//                RandTransSel = 1
//                VelocityCheck = 1
//                floorLimit = jsonObject.tflDomestic22
//                threshold = jsonObject.offlineThresholdDomestic24
                tacDenial = jsonObject.tacDenial30
                tacOnline = jsonObject.tacOnline31
                tacDefault = jsonObject.defaultTacValue29
//                AcquierId = byteArrayOf(1, 35, 69, 103, -119, 16)
                setDdol(jsonObject.ddol20.prependLength)
//                TDOL = jsonObject.tdol21.prependLength.hexBytes
                appVerNum = jsonObject.appVersion18
//                RiskManData =
//                    byteArrayOf(0x6C, 0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            }
            aidEntityList.add(aidEntity)
        }
        aidEntityList.addAll(getDefaultAidList(activity))
        emvHandler2.setAidParaList(aidEntityList)
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
                checkSum = jsonObject.hash39
            }
            capkEntityList.add(capkEntity)
        }
        capkEntityList.addAll(getDefaultCapkList(activity))
        emvHandler2.setCAPKList(capkEntityList)
    }

    override fun cleanUpEmv() {
        sessionData.reset()
    }

    private fun PosManager.SessionData.reset() {
        amount = 0L
        pinBlock = null
        canRunTransaction = true
        canManageParameters = false
        transactionType = TransactionType.Unknown
    }

    companion object : PosManagerCompanion {
        override fun setup(context: Context) {
            APIProxy.getDeviceEngine(context).getEmvHandler2("app2")
        }

        override val module = module(override = true) {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                N3PosManager(activity)
            }
            factory<PosPrinter>(override = true) { (activity: Activity, dialogProvider: DialogProvider) ->
                N3Printer(activity, dialogProvider)
            }
        }

        override fun isCompatible(context: Context): Boolean = try {
            APIProxy.getDeviceEngine(context).getEmvHandler2("app2")
            true
        } catch (ex: Exception) {
            debugOnly { Log.e("N3PosManager", ex.message, ex) }
            false
        } catch (err: UnsatisfiedLinkError) {
            debugOnly { Log.e("N3PosManager", err.message, err) }
            false
        }
    }
}