package com.appzonegroup.creditclub.pos

import android.content.Context
import android.content.Intent
import com.appzonegroup.creditclub.pos.service.SyncService
import com.creditclub.core.util.isMyServiceRunning


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */

fun Context.startPosApp() {

    if (!isMyServiceRunning(SyncService::class.java)) {
        startService(Intent(this, SyncService::class.java))
    }
}