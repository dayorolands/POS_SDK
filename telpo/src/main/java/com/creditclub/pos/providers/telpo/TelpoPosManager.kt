package com.creditclub.pos.providers.telpo

import android.content.Context
import android.util.Log
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosParameter
import com.creditclub.pos.TransactionResponse
import com.creditclub.pos.extensions.*
import com.creditclub.pos.printer.PosPrinter
import com.telpo.emv.EmvApp
import com.telpo.emv.EmvCAPK
import com.telpo.emv.EmvService
import com.telpo.pinpad.PinpadService
import com.telpo.tps550.api.util.SystemUtil
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.dsl.module
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class TelpoPosManager(private val activity: CreditClubActivity) : PosManager, KoinComponent {
    override val cardReader by lazy { TelpoCardReader(activity, this) }
    private val posParameter: PosParameter by inject()

    internal val emvService by lazy { EmvService.getInstance() }
    override val sessionData = PosManager.SessionData()

    override suspend fun loadEmv() {
        safeRun { PinpadService.Close() }

        EmvService.Emv_SetDebugOn(if (BuildConfig.DEBUG) 1 else 0)

        StartEmvService(activity).run()
        StartPinPadService(activity, get()).run()

        EmvService.Emv_RemoveAllApp()
        EmvService.Emv_RemoveAllCapk()

        injectAid()
        StableAPPCAPK.Add_All_APP()
        injectCapk()
        StableAPPCAPK.Add_All_CAPK()
    }

    override fun cleanUpEmv() {
        PinpadService.Close()
        EmvService.deviceClose()
    }

    override suspend fun startTransaction(): TransactionResponse {
        throw NotImplementedError("An operation is not implemented")
    }

    private fun injectAid() {
        val jsonArray = posParameter.emvAidList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength) {
            val jsonObject = jsonArray.getJSONObject(i)
            val emvApp = EmvApp()
            emvApp.AppName = jsonObject.appName17.toByteArray(StandardCharsets.US_ASCII)
            emvApp.AID = jsonObject.aid15.hexBytes
            emvApp.SelFlag = 0
            emvApp.Priority = jsonObject.selectionPriority19.hexByte
            emvApp.TargetPer = jsonObject.targetPercentageDomestic27.hexByte
            emvApp.MaxTargetPer = jsonObject.maxTargetDomestic25.hexByte
            emvApp.FloorLimitCheck = 1
            emvApp.RandTransSel = 1
            emvApp.VelocityCheck = 1
            emvApp.FloorLimit = jsonObject.tflDomestic22.hexBytes
            emvApp.Threshold = jsonObject.offlineThresholdDomestic24.hexBytes
            emvApp.TACDenial = jsonObject.tacDenial30.hexBytes
            emvApp.TACOnline = jsonObject.tacOnline31.hexBytes
            emvApp.TACDefault = jsonObject.defaultTacValue29.hexBytes
            emvApp.AcquierId = byteArrayOf(1, 35, 69, 103, -119, 16)
            emvApp.DDOL = jsonObject.ddol20.prependLength.hexBytes
            emvApp.TDOL = jsonObject.tdol21.prependLength.hexBytes
            emvApp.Version = jsonObject.appVersion18.hexBytes
            emvApp.RiskManData =
                byteArrayOf(0x6C, 0xFF.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            EmvService.Emv_AddApp(emvApp)
        }
    }

    private inline val String.prependLength get() = "${length / 2}${this}"

    private fun injectCapk() {
        val jsonArray = posParameter.capkList ?: return
        val arrayLength = jsonArray.length()
        for (i in 0 until arrayLength) {
            val jsonObject = jsonArray.getJSONObject(i)
            val capk = EmvCAPK()
            capk.RID = jsonObject.rid35.hexBytes
            capk.KeyID = jsonObject.keyIndex32.hexByte
            capk.HashInd = jsonObject.hashAlgorithm36.hexByte
            capk.ArithInd = jsonObject.keyAlgorithm40.hexByte
            capk.Modul = jsonObject.modulus37.replace("\n", "").hexBytes
            capk.Exponent = jsonObject.exponent38.hexBytes
            capk.ExpDate = byteArrayOf(37, 18, EmvService.TYPE_BALANCE_INQUIRY)
            capk.CheckSum = jsonObject.hash39.hexBytes
            EmvService.Emv_AddCapk(capk)
        }
    }

    companion object : PosManagerCompanion {
        private var deviceType = -1

        override val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                TelpoPosManager(activity)
            }
            factory<PosPrinter>(override = true) { (context: Context, dialogProvider: DialogProvider) ->
                TelpoPrinter(context, dialogProvider)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            try {
                deviceType = SystemUtil.getDeviceType()
                return true
            } catch (ex: Exception) {
                debugOnly { Log.e("TelpoPosManager", ex.message, ex) }
            } catch (err: UnsatisfiedLinkError) {
                debugOnly { Log.e("TelpoPosManager", err.message, err) }
            }

            return false
        }

        override fun setup(context: Context) {
//            val testFolder = Environment.getExternalStorageDirectory().path
//
//            CoroutineScope(Dispatchers.Main).launch {
//                try {
//                    context.copyAssetToFolder("pinpad_res.zip", "$testFolder/test", "res.zip")
//                } catch (ex: Exception) {
//                    ex.printStackTrace()
//                    return@launch
//                }
//
//                val setResIntent = Intent("android.intent.action.emv.res.set")
//                setResIntent.putExtra("emv_res", "$testFolder/res.zip")
//                context.sendBroadcast(setResIntent)
//            }
        }

        @Throws(Exception::class)
        private fun Context.copyAssetToFolder(
            assetName: String,
            savePath: String,
            saveName: String
        ) {
            val filename = "$savePath/$saveName"
            val dir = File(savePath)

            if (!dir.exists()) {
                dir.mkdir()
            }

            val inputStream = resources.assets.open(assetName)
            val fileOutputStream = FileOutputStream(filename)
            val buffer = ByteArray(7168)

            while (true) {
                val read = inputStream.read(buffer)

                if (read > 0) {
                    fileOutputStream.write(buffer, 0, read)
                } else {
                    fileOutputStream.close()
                    inputStream.close()
                    return
                }
            }
        }
    }
}