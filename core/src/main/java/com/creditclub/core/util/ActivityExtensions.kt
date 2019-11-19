package com.creditclub.core.util

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import com.creditclub.core.ui.widget.DialogListenerBlock


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