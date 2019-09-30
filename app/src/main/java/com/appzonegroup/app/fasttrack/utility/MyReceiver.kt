package com.appzonegroup.app.fasttrack.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.creditclub.core.util.isMyServiceRunning

/**
 * Created by Joseph on 6/5/2016.
 */
class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!context.isMyServiceRunning(SyncService::class.java)) {
            val i = Intent(context, SyncService::class.java)

            try {
                context.startService(i)
            } catch (ex: IllegalStateException) {
                ex.printStackTrace()
                Crashlytics.logException(ex)

                i.putExtra("NEED_FOREGROUND_KEY", true)

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (ex2: IllegalStateException) {
                    ex2.printStackTrace()
                    Crashlytics.logException(ex2)
                }

            }
        }
    }
}
