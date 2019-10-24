package com.appzonegroup.app.fasttrack.utility

import android.app.ActivityManager
import android.content.Context
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

object LogOutTimerUtil {

    internal var longTimer: Timer? = null
    private const val LOGOUT_TIME = 600000

    interface LogOutListener {
        fun doLogout()
    }

    @Synchronized
    fun startLogoutTimer(context: Context, logOutListener: LogOutListener) {
        if (longTimer != null) {
            longTimer!!.cancel()
            longTimer = null
        }
        if (longTimer == null) {

            longTimer = Timer()

            longTimer!!.schedule(object : TimerTask() {

                override fun run() {

                    cancel()

                    longTimer = null

                    GlobalScope.launch(Dispatchers.Main) {
                        val (foreGround) = safeRunIO {
                            val activityManager =
                                context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

                            val appProcesses =
                                activityManager.runningAppProcesses ?: return@safeRunIO false

                            val packageName = context.applicationContext.packageName

                            for (appProcess in appProcesses) {
                                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                                    return@safeRunIO true
                                }
                            }

                            return@safeRunIO false
                        }

                        if (foreGround == true) {
                            logOutListener.doLogout()
                        }
                    }
                }
            }, LOGOUT_TIME.toLong())
        }
    }

    @Synchronized
    fun stopLogoutTimer() {
        if (longTimer != null) {
            longTimer!!.cancel()
            longTimer = null
        }
    }
}
