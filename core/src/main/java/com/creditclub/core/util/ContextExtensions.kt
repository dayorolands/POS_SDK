package com.creditclub.core.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RawRes
import com.creditclub.core.BuildConfig
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.model.AppFunctionUsage
import com.creditclub.core.data.model.DeviceTransactionInformation
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.type.TransactionCountType
import com.creditclub.core.util.delegates.defaultJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */

inline val Context.localStorage get() = LocalStorage(this)
inline val Context.appDataStorage get() = AppDataStorage.getInstance(this)
inline val Context.coreDatabase: CoreDatabase get() = CoreDatabase.getInstance(this)
inline val Context.application get() = applicationContext as CreditClubApplication

fun Context.getTransactionMonitorCounter(key: String): Int {
    val localStorage = localStorage
    val value = localStorage.getString(key)
    var count = 0
    if (value != null) count = Integer.parseInt(value)

    localStorage.putString(key, count.toString())
    return count
}

fun Context.increaseTransactionMonitorCounter(
    transactionCountType: TransactionCountType,
    sessionID: String
) {
    val localStorage = localStorage
    val value = localStorage.getString(transactionCountType.key)
    var count = 0
    if (value != null) count = Integer.parseInt(value) + 1

    localStorage.putString(transactionCountType.key, count.toString())

    GlobalScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) {
            val info = DeviceTransactionInformation.getInstance(
                this@increaseTransactionMonitorCounter,
                sessionID
            )
            coreDatabase.deviceTransactionInformationDao().save(info)
        }
    }
}

fun Context.resetTransactionMonitorCounter() {
    TransactionCountType.values().forEach {
        localStorage.putString(it.key, "0")
    }
}

inline val Context.appVersionName: String
    get() {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return ""
    }

inline val Context.packageInfo: PackageInfo?
    get() {
        return try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            if (BuildConfig.DEBUG) e.printStackTrace()
            null
        }
    }

inline val Context.RAMInfo: LongArray
    get() {
        val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return longArrayOf(memInfo.availMem, memInfo.totalMem)
    }

suspend inline fun Context.logFunctionUsage(fid: Int) = safeRunIO {
    val count = run {
        val appFunctionUsageDao = coreDatabase.appFunctionUsageDao()
        val appFunction = appFunctionUsageDao.getFunction(fid)

        if (appFunction == null) {
            appFunctionUsageDao.insert(AppFunctionUsage(fid))

            return@run 1
        } else {
            appFunction.usage++
            appFunctionUsageDao.update(appFunction)

            return@run appFunction.usage
        }
    }

    if (BuildConfig.DEBUG) Log.d("AppUsage", "Usage for function $fid -> $count")
}

inline fun <reified T : Any> Context.readRawJsonFile(
    @RawRes fileLocation: Int,
    serializer: KSerializer<T> = serializer()
): T {
    val fileContents =
        resources.openRawResource(fileLocation).bufferedReader().use { it.readText() }
    return defaultJson.decodeFromString(serializer, fileContents)
}
