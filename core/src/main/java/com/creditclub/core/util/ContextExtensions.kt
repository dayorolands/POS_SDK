package com.creditclub.core.util

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import com.creditclub.core.BuildConfig
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.model.AppFunctionUsage
import com.creditclub.core.data.model.DeviceTransactionInformation
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.type.TransactionCountType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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

suspend inline fun Context.increaseTransactionMonitorCounter(
    transactionCountType: TransactionCountType,
    sessionID: String
) {
    val localStorage = localStorage
    val value = localStorage.getString(transactionCountType.key)
    var count = 0
    if (value != null) count = Integer.parseInt(value) + 1

    localStorage.putString(transactionCountType.key, count.toString())

    withContext(Dispatchers.IO) {
        val info = DeviceTransactionInformation.getInstance(
            this@increaseTransactionMonitorCounter,
            sessionID
        )
        coreDatabase.deviceTransactionInformationDao().save(info)
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