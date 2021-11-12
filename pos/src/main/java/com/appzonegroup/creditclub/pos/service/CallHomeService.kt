package com.appzonegroup.creditclub.pos.service

import android.content.Context
import androidx.work.*
import com.appzonegroup.creditclub.pos.extension.processingCode3
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.creditclub.pos.PosConfig
import com.creditclub.pos.work.CallHomeWorker
import kotlinx.coroutines.*
import org.jpos.iso.ISOMsg
import org.koin.ext.getFullName
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


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