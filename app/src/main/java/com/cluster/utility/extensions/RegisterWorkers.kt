package com.cluster.utility.extensions

import android.app.Application
import androidx.work.*
import com.cluster.work.MobileTrackingWorker
import org.koin.ext.getFullName
import java.util.concurrent.TimeUnit

fun Application.registerWorkers() {
    val workManager = WorkManager.getInstance(this)
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    workManager.registerPeriodicWorker<MobileTrackingWorker>(
        uniqueWorkName = "APP_MOBILE_TRACKING",
        constraints = constraints,
    )
}

inline fun <reified W : ListenableWorker> WorkManager.registerPeriodicWorker(
    constraints: Constraints,
    uniqueWorkName: String = W::class.getFullName(),
) {
    val periodicWorkRequest = PeriodicWorkRequestBuilder<W>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

    enqueueUniquePeriodicWork(
        uniqueWorkName,
        ExistingPeriodicWorkPolicy.REPLACE,
        periodicWorkRequest,
    )
}