package com.appzonegroup.creditclub.pos

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.appzonegroup.creditclub.pos.provider.telpo.TelpoPosManager
import com.creditclub.pos.PosProviders
import com.telpo.tps550.api.util.StringUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/29/2019.
 * Appzone Ltd
 */
object Platform : KoinComponent {

    @JvmStatic
    var isPOS = false
        private set

    @JvmStatic
    val hasPrinter
        get() = supportsPrinter()

    @JvmStatic
    fun supportsPrinter(): Boolean {
        if (TelpoPosManager.deviceType == -1) return false

        return TelpoPosManager.deviceType == StringUtil.DeviceModelEnum.TPS900.ordinal
    }

    @JvmStatic
    fun setup(context: Context) {
        val testFolder = Environment.getExternalStorageDirectory().path

        CoroutineScope(Dispatchers.Main).launch {
            try {
                context.copyAssetToFolder("pinpad_res.zip", "$testFolder/test", "res.zip")
            } catch (ex: Exception) {
                ex.printStackTrace()
                return@launch
            }

            val setResIntent = Intent("android.intent.action.emv.res.set")
            setResIntent.putExtra("emv_res", "$testFolder/res.zip")
            context.sendBroadcast(setResIntent)
        }
    }

    fun test(application: Application) {
        for (posManagerCompanion in PosProviders.registered) {
            if (posManagerCompanion.isCompatible(application)) {
                isPOS = true
                posManagerCompanion.setup(application)
                loadKoinModules(posManagerCompanion.module)
                loadPosModules()
                application.startPosApp()
                return
            }
        }

        if (TelpoPosManager.isCompatible(application)) {
            isPOS = true
            loadKoinModules(TelpoPosManager.module)
        }

        if (isPOS) {
            loadPosModules()
            application.startPosApp()
        }
    }

    @Throws(Exception::class)
    fun Context.copyAssetToFolder(assetName: String, savePath: String, saveName: String) {
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
