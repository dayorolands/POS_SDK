package com.creditclub.core.util

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import com.creditclub.core.R
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.ui.widget.DialogListenerBlock
import org.koin.android.ext.android.get


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/20/2019.
 * Appzone Ltd
 */

fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager?
    manager?.run {
        for (service in getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
    }

    return false
}

val Activity.finishOnClose: DialogListenerBlock<Nothing>
    get() = {
        onClose {
            finish()
        }
    }

suspend inline fun Activity.getLatestVersion() = safeRun {
    val creditClubMiddleWareAPI: CreditClubMiddleWareAPI = get()

    val appName = getString(R.string.ota_app_name)
    val version = creditClubMiddleWareAPI.versionService.getLatestVersionAndDownloadLink(appName)

    if (version != null) {
        appDataStorage.latestVersion = version
    }

    version
}