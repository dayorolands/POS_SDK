package com.appzonegroup.creditclub.pos

import android.content.Context
import android.content.Intent
import android.os.Environment
import com.telpo.tps550.api.util.StringUtil
import com.telpo.tps550.api.util.SystemUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/29/2019.
 * Appzone Ltd
 */
object Platform {
    private var deviceType = -1

    @JvmStatic
    val isPOS
        get() = supportsPos()

    @JvmStatic
    val hasPrinter
        get() = supportsPrinter()

    init {
        try {
            deviceType = SystemUtil.getDeviceType()
        } catch (ex: Exception) {
            if (BuildConfig.DEBUG) ex.printStackTrace()
        } catch (err: UnsatisfiedLinkError) {
            if (BuildConfig.DEBUG) err.printStackTrace()
        }
    }

    @JvmStatic
    fun supportsPos(): Boolean {
        if (deviceType == -1) return false

        return deviceType == StringUtil.DeviceModelEnum.TPS450C.ordinal || deviceType == StringUtil.DeviceModelEnum.TPS360IC.ordinal || deviceType == StringUtil.DeviceModelEnum.TPS900.ordinal
    }

    @JvmStatic
    fun supportsPrinter(): Boolean {
        if (deviceType == -1) return false

        return deviceType == StringUtil.DeviceModelEnum.TPS900.ordinal
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
