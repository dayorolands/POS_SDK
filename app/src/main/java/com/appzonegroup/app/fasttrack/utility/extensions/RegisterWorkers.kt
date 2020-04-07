package com.appzonegroup.app.fasttrack.utility.extensions

import android.app.Application
import androidx.work.*
import com.appzonegroup.app.fasttrack.work.MobileTrackingWorker
import com.appzonegroup.app.fasttrack.work.PosNotificationWorker
import com.appzonegroup.creditclub.pos.Platform
import java.util.concurrent.TimeUnit

fun Application.registerWorkers() {

    val workManager = WorkManager.getInstance(this)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val mobileTrackingRequest =
        PeriodicWorkRequestBuilder<MobileTrackingWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

    workManager.enqueueUniquePeriodicWork(
        "MOBILE_TRACKING",
        ExistingPeriodicWorkPolicy.REPLACE,
        mobileTrackingRequest
    )

    if (Platform.isPOS) {
        val posNotificationRequest =
            PeriodicWorkRequestBuilder<PosNotificationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        workManager.enqueueUniquePeriodicWork(
            "POS_NOTIFICATION",
            ExistingPeriodicWorkPolicy.REPLACE,
            posNotificationRequest
        )
    }
}