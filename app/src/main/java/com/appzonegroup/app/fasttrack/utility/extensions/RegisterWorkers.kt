package com.appzonegroup.app.fasttrack.utility.extensions

import android.app.Application
import androidx.work.*
import com.appzonegroup.app.fasttrack.work.AppUpdateWorker
import com.appzonegroup.app.fasttrack.work.MobileTrackingWorker
import com.appzonegroup.app.fasttrack.work.PosNotificationWorker
import com.appzonegroup.creditclub.pos.Platform
import org.koin.ext.getFullName
import java.util.concurrent.TimeUnit

fun Application.registerWorkers() {
    val workManager = WorkManager.getInstance(this)
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    workManager.registerPeriodicWorker<MobileTrackingWorker>(
        uniqueWorkName = "APP_MOBILE_TRACKING",
        constraints = constraints,
    )

    if (Platform.isPOS) {
        workManager.registerPeriodicWorker<PosNotificationWorker>(
            uniqueWorkName = "APP_POS_NOTIFICATION",
            constraints = constraints,
        )
    }
    val hasPosUpdateManager = Platform.isPOS && Platform.deviceType != 2
    if (hasPosUpdateManager) {
        workManager.registerPeriodicWorker<AppUpdateWorker>(
            constraints = constraints,
        )
    }
}

inline fun <reified W : ListenableWorker> WorkManager.registerPeriodicWorker(
    uniqueWorkName: String = W::class.getFullName(),
    constraints: Constraints,
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