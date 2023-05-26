package com.creditclub.pos.providers.newland

import android.content.Context
import android.os.Build
import android.util.Log
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.safeRun
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardReaders
import com.cluster.pos.extensions.aid15
import com.cluster.pos.extensions.defaultTacValue29
import com.cluster.pos.extensions.exponent38
import com.cluster.pos.extensions.hash39
import com.cluster.pos.extensions.hashAlgorithm36
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.extensions.keyAlgorithm40
import com.cluster.pos.extensions.keyIndex32
import com.cluster.pos.extensions.modulus37
import com.cluster.pos.extensions.rid35
import com.cluster.pos.extensions.tacDenial30
import com.cluster.pos.extensions.tacOnline31
import com.creditclub.pos.providers.newland.util.EmvL3Configuration
import com.creditclub.pos.providers.newland.util.Singletons
import com.newland.nsdk.core.api.common.utils.LogLevel
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl
import com.newland.sdk.emvl3.api.common.EmvL3Const
import com.newland.sdk.emvl3.api.common.EmvL3Const.CardInterface
import com.newland.sdk.emvl3.api.common.configuration.CAPKEntry
import com.newland.sdk.emvl3.api.internal.configuration.AID
import com.newland.sdk.emvl3.api.internal.configuration.CAPK
import com.newland.sdk.emvl3.internal.configuration.AidImpl
import com.newland.sdk.emvl3.internal.configuration.CapkImpl
import com.newland.sdk.emvl3.internal.transaction.EmvL3Impl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewlandPosManager(
    private val ordaActivity: CreditClubActivity,
) : PosManager, KoinComponent{
    private val nsdkModuleManager = NSDKModuleManagerImpl.getInstance()
    private val emvL3Module = EmvL3Impl()
    private val emvL3Configuration = EmvL3Configuration()
    private val posParameter : PosParameter by inject()
    override val sessionData = PosManager.SessionData()
    override val cardReader: CardReaders by lazy {
        NewlandCardReader(
            ordaActivity = ordaActivity,
            emvL3 = emvL3Module,
            nsdkModuleManager = nsdkModuleManager,
            sessionData = sessionData
        )
    }

    override suspend fun loadEmv() {
        delay(1000)
        withContext(Dispatchers.Main) {
            suspendCoroutine<Unit> { continuation ->
                nsdkModuleManager.init(ordaActivity)
                nsdkModuleManager.setDebugMode(LogLevel.DEBUG)

                // Configure the EMV L3 module
                val config = ByteArray(10)
                emvL3Module.configUnSet(config, EmvL3Const.CONFIG.L3_CFG_SUPPORT_SM)
                emvL3Module.configUnSet(config, EmvL3Const.CONFIG.L3_CFG_SUPPORT_EC)
                val initialValue = emvL3Module.init("${ordaActivity.filesDir}${File.separator}${"emv"}${File.separator}", config)
                if(initialValue < 0){
                    Log.d(javaClass.name, String.format("m_EmvL3Module.init() returned %d", initialValue))
                }
                emvL3Module.setDebugMode(3)
                emvL3Module.setConfig(EmvL3Const.CONFIG.L3_CFG_SUPPORT_GET_RFAPDU, 0x01)

                Singletons.setEmvL3(emvL3Module)
                Singletons.setNsdkModuleManager(nsdkModuleManager)

                safeRun {
                    emvL3Configuration.LoadL3EmvConfiguration()
                    injectAid()
                    injectCapk()
                }
                continuation.resume(Unit)
            }
        }
    }

    private fun injectAid(){
        val aidList : AID = AidImpl(CardInterface.CONTACT)
        val jsonArray = posParameter.emvAidList ?: return
        val arrayLength = jsonArray.length()
        for(i in 0 until arrayLength){
            val jsonObject = jsonArray.getJSONObject(i)
            val paddedAid15 = "9F06${jsonObject.aid15.getTLVLength()}${jsonObject.aid15}" +
                    "DF13${jsonObject.tacDenial30.getTLVLength()}${jsonObject.tacDenial30}" +
                    "DF12${jsonObject.tacOnline31.getTLVLength()}${jsonObject.tacOnline31}" +
                    "DF11${jsonObject.defaultTacValue29.getTLVLength()}${jsonObject.defaultTacValue29}" +
                    "9F0902008C" + "9F1B0400000000" + "9F530152" + "1F811F0A9F0AFFFFFFFF02030320"
            val aidInjection = aidList.loadAID(paddedAid15.hexBytes)
            if(aidInjection < 0){
                Log.d(javaClass.name, String.format("AidList.load returned %d", aidInjection))
            }
        }
    }

    private fun String.getTLVLength() = String.format("%02d", this.length.div(2))

    private fun injectCapk(){
        val addCapk : CAPK = CapkImpl()
        val jsonArray = posParameter.capkList ?: return
        val arrayLength = jsonArray.length()
        for(i in 0 until arrayLength){
            val jsonObject = jsonArray.getJSONObject(i)
            val capkEntry = CAPKEntry()
            capkEntry.rid = jsonObject.rid35.hexBytes
            capkEntry.index = jsonObject.keyIndex32.toInt()
            capkEntry.hash = jsonObject.hash39.hexBytes
            capkEntry.hashAlgorithm = jsonObject.hashAlgorithm36.toByte()
            capkEntry.exponent = jsonObject.exponent38.hexBytes
            capkEntry.algorithmIndicator = jsonObject.keyAlgorithm40.toByte()
            capkEntry.modulus = jsonObject.modulus37.replace("\n", "").hexBytes
            capkEntry.expiredDate = "20311222".hexBytes
            capkEntry.moduleLen = jsonObject.modulus37.replace("\n", "").hexBytes.size
            val capkInjection = addCapk.load(capkEntry)
            if(capkInjection < 0){
                Log.d(javaClass.name, String.format("capkLoader.load returned %d", capkInjection))
            }
        }
    }

    override fun cleanUpEmv() {
        emvL3Module.terminateTransaction()
    }

    companion object : PosManagerCompanion{
        override val id: String = "NewlandPOS"
        override val deviceType: Int = 5
        override val module: Module = module {
            factory<PosManager>{ (activity : CreditClubActivity) ->
                NewlandPosManager(activity)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            val manufacturerName = Build.MANUFACTURER
            return manufacturerName.contains("newland")
        }

        override fun setup(context: Context) {
        }

    }
}