package com.appzonegroup.creditclub.pos.service

import android.content.Context
import androidx.work.*
import com.creditclub.pos.work.CallHomeWorker
import org.koin.ext.getFullName
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/3/2019.
 * Appzone Ltd
 */

class CallHomeService(private val context: Context) {

    private var isCallHomeTimerRunning = false
    private val uniqueWorkName = CallHomeWorker::class.getFullName()

    fun startCallHomeTimer() {
        if (!isCallHomeTimerRunning) {
            isCallHomeTimerRunning = !isCallHomeTimerRunning

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicWorkRequest =
                PeriodicWorkRequestBuilder<CallHomeWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                uniqueWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest,
            )
        }
    }

    fun stopCallHomeTimer() {
        if (isCallHomeTimerRunning) {
            isCallHomeTimerRunning = !isCallHomeTimerRunning
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }
}