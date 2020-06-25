package com.creditclub.pos.providers.telpo

import android.content.Context
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.printer.PosPrinter
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.safeRun
import com.telpo.emv.EmvService
import com.telpo.pinpad.PinpadService
import com.telpo.tps550.api.util.SystemUtil
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.dsl.module
import java.io.File
import java.io.FileOutputStream


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class TelpoPosManager(val activity: CreditClubActivity) : PosManager, KoinComponent {
    override val cardReader by lazy { TelpoCardReader(activity, emvListener) }

    private val emvListener by lazy {
        TelpoEmvListener(activity, emvService, sessionData)
    }

    private val emvService by lazy { EmvService.getInstance() }
    override val sessionData = PosManager.SessionData()

    override suspend fun loadEmv() {
        safeRun { PinpadService.Close() }

        emvService.setListener(emvListener)

        EmvService.Emv_SetDebugOn(if (BuildConfig.DEBUG) 1 else 0)

        StartEmvService(activity).run()
        StartPinPadService(activity, get()).run()

        EmvService.Emv_RemoveAllApp()
        EmvService.Emv_RemoveAllCapk()

        StableAPPCAPK.Add_All_APP()
        StableAPPCAPK.Add_All_CAPK()
    }

    override fun cleanUpEmv() {
        PinpadService.Close()
        EmvService.deviceClose()
    }

    companion object : PosManagerCompanion {
        var deviceType = -1
            private set

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
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) ex.printStackTrace()
            } catch (err: UnsatisfiedLinkError) {
                if (BuildConfig.DEBUG) err.printStackTrace()
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