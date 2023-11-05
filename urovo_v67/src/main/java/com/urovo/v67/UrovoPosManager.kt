package com.urovo.v67

import android.content.Context
import android.os.Build
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.safeRun
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardReaders
import com.cluster.pos.extensions.aid15
import com.cluster.pos.extensions.appName17
import com.cluster.pos.extensions.appVersion18
import com.cluster.pos.extensions.ddol20
import com.cluster.pos.extensions.exponent38
import com.cluster.pos.extensions.hash39
import com.cluster.pos.extensions.hashAlgorithm36
import com.cluster.pos.extensions.keyAlgorithm40
import com.cluster.pos.extensions.keyIndex32
import com.cluster.pos.extensions.maxTargetDomestic25
import com.cluster.pos.extensions.modulus37
import com.cluster.pos.extensions.offlineThresholdDomestic24
import com.cluster.pos.extensions.rid35
import com.cluster.pos.extensions.targetPercentageDomestic27
import com.cluster.pos.extensions.tdol21
import com.cluster.pos.printer.PosPrinter
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.v67.aidUtils.AIDBean
import com.urovo.v67.aidUtils.CAPKBean
import com.urovo.v67.aidUtils.EmvUtils
import com.urovo.v67.aidUtils.IccParamsInitUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UrovoPosManager(
    private val activity: CreditClubActivity,
) : PosManager, KoinComponent{
    private val mKernelApi = EmvNfcKernelApi.getInstance()
    override val sessionData = PosManager.SessionData()
    private val posParameter: PosParameter by inject()
    override val cardReader: CardReaders by lazy {
        UrovoCardReader(
            activity = activity,
            sessionData = sessionData,
            emvNfcKernelApi = mKernelApi
        )
    }

    override suspend fun loadEmv() {
        delay(2000)
        withContext(Dispatchers.Main){
            suspendCoroutine<Unit> { continuation ->
                mKernelApi.setContext(activity)
                IccParamsInitUtil.updateAID(activity, IccParamsInitUtil.getInitAIDParams())
                IccParamsInitUtil.updateRID(activity, IccParamsInitUtil.getInitCAPKParams())
                updateAID()
                updateCAPK()

                continuation.resume(Unit)
            }
        }
    }


    private fun updateAID() {
        val jsonArray = posParameter.emvAidList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength){
            val jsonObject = jsonArray.getJSONObject(i)
            val aidBean = AIDBean()
            aidBean.aid = jsonObject.aid15
            aidBean.aidLable = jsonObject.appName17
            aidBean.setTerminalAIDVersionNumber(jsonObject.appVersion18)
            aidBean.setExactOnlySelection("0")
            aidBean.setSkipEMVProgressing("0")
            aidBean.defaultTDOL = jsonObject.tdol21
            aidBean.defaultDDOL = jsonObject.ddol20
            aidBean.setDenialActionCode("0010000000")
            aidBean.setOnlineActionCode("DC4004F800")
            aidBean.setDefaultActionCode("0010000000")
            aidBean.setThresholdValue(jsonObject.offlineThresholdDomestic24)
            aidBean.setTargetPercebtage(jsonObject.targetPercentageDomestic27)
            aidBean.setMaximumTargetPercent(jsonObject.maxTargetDomestic25)


            EmvUtils.updateAIDForContact(aidBean)
        }
    }

    private fun updateCAPK(){
        val jsonArray = posParameter.capkList ?: return
        val arrayLength = jsonArray.length()
        for(i in 0 until arrayLength){
            val jsonObject = jsonArray.getJSONObject(i)
            val capkBean = CAPKBean()
            capkBean.rid = jsonObject.rid35
            capkBean.cA_PKIndex = jsonObject.keyIndex32
            capkBean.cA_HashAlgoIndicator = jsonObject.hashAlgorithm36
            capkBean.cA_PKAlgoIndicator = jsonObject.keyAlgorithm40
            capkBean.capkExponent = jsonObject.exponent38
            capkBean.checksumHash = jsonObject.hash39
            capkBean.capkModulus = jsonObject.modulus37
            capkBean.capkExpDate = "251231"

            EmvUtils.updateCAPK(capkBean)
        }
    }

    override fun cleanUpEmv() {
        mKernelApi.abortKernel()
    }


    companion object : PosManagerCompanion {
        override val id: String = "UrovoPOS"
        override val deviceType: Int = 9
        override val module: Module = module {
            factory<PosManager>{ (activity : CreditClubActivity) ->
                UrovoPosManager(activity)
            }
            factory<PosPrinter> { (context: Context, dialogProvider: DialogProvider) ->
                UrovoPrinter(
                    context,
                    dialogProvider
                )
            }
        }

        override fun isCompatible(context: Context): Boolean {
            val manufacturerName = Build.BRAND
            return manufacturerName.contains("urovo", ignoreCase = true)
        }

        override fun setup(context: Context) {
        }

    }
}