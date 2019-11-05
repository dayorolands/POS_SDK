package com.creditclub.core.util

import android.app.ActivityManager
import android.app.Application
import android.content.Context


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