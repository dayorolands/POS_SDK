package com.creditclub.pos.providers.telpo

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/25/2019.
 * Appzone Ltd
 */
class WakeUpAndUnlock(val context: Context) : Runnable {
    override fun run() {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val kl = km.newKeyguardLock("unLock")
        kl.disableKeyguard()
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl =
            pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK, "pre:bright")
        wl.acquire(10 * 60 * 1000L /*10 minutes*/)
        wl.release()
    }
}