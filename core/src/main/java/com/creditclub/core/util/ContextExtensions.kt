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

inline val Context.localStorage get() = LocalStorage.getInstance(this)
inline val Context.appDataStorage get() = AppDataStorage.getInstance(this)
inline val Context.coreDatabase: CoreDatabase get() = CoreDatabase.getInstance(this)
inline val Context.application get() = applicationContext as CreditClubApplication

fun Context.getTransactionMonitorCounter(key: String): Int {
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

//val Context.mainMenuItems: ArrayList<MainMenuItem>
//    get() {
//        val mainMenuItems = ArrayList<MainMenuItem>()
//        mainMenuItems.add(MainMenuItem(R.drawable.open_account, null))
//        mainMenuItems.add(MainMenuItem(R.drawable.cash_deposit, null))
//        mainMenuItems.add(MainMenuItem(R.drawable.cash_withdrawal, null))
//        // mainMenuItems.add(new MainMenuItem(R.drawable.bvn_update, null));
//        mainMenuItems.add(MainMenuItem(R.drawable.loan_request, null))
//        // mainMenuItems.add(new MainMenuItem(R.drawable.funds_transfer, null));
//        mainMenuItems.add(MainMenuItem(R.drawable.conditional_cash_transfer, null))
//        mainMenuItems.add(MainMenuItem(R.drawable.bills_payment, null))
//
//        // New Menu items
//        mainMenuItems.add(MainMenuItem(R.drawable.interbank_withdrawal, "InterBank Withdrawal"))
//        mainMenuItems.add(MainMenuItem(R.drawable.intrabank_withdrawal, "Same bank Withdrawal"))
//
//        if (packageName.toLowerCase().contains("farepay")) {
//            // this is for link card... it should be changed to the right image when ready
//            mainMenuItems.add(MainMenuItem(R.drawable.bg_min, "Link Card"))
//
//            // this is for hotlist card... it should be changed to the right image when
//            // ready
//            mainMenuItems.add(MainMenuItem(R.drawable.logo, "Hotlist card"))
//        }
//
//        return mainMenuItems
//    }
//
//val Context.cashOutMainMenuItems: ArrayList<MainMenuItem>
//    get() {
//        val mainMenuItems = ArrayList<MainMenuItem>()
//        mainMenuItems.add(MainMenuItem(R.drawable.ic_mail, "Token Cash-Out"))
//        mainMenuItems.add(MainMenuItem(R.drawable.ic_credit_card, "Card cash-Out"))
//        mainMenuItems.add(MainMenuItem(R.drawable.ic_money, "Funds Transfer"))
//
//        // New Menu items
//        /*
//         * mainMenuItems.add(new MainMenuItem(R.drawable.interbank_withdrawal,
//         * "InterBank Withdrawal")); mainMenuItems.add(new
//         * MainMenuItem(R.drawable.intrabank_withdrawal, "Same bank Withdrawal"));
//         */
//
//        return mainMenuItems
//    }

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