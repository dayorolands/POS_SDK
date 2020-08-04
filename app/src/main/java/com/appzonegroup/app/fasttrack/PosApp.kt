package com.appzonegroup.app.fasttrack

import android.app.Application
import androidx.work.*
import com.appzonegroup.app.fasttrack.work.IsoRequestLogWorker
import com.appzonegroup.app.fasttrack.work.ReversalWorker
import com.appzonegroup.app.fasttrack.work.TransactionLogWorker
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.dsl.module
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */

fun Application.startPosApp() {
    val workManager = WorkManager.getInstance(this)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val transactionLogRequest =
        PeriodicWorkRequestBuilder<TransactionLogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

    workManager.enqueueUniquePeriodicWork(
        "TRANSACTION_LOG",
        ExistingPeriodicWorkPolicy.REPLACE,
        transactionLogRequest
    )

    workManager.enqueueUniquePeriodicWork(
        "ISO_REQUEST_LOG",
        ExistingPeriodicWorkPolicy.REPLACE,
        PeriodicWorkRequestBuilder<IsoRequestLogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
    )

    workManager.enqueueUniquePeriodicWork(
        "REVERSAL",
        ExistingPeriodicWorkPolicy.REPLACE,
        PeriodicWorkRequestBuilder<ReversalWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
    )

    if (get<ConfigService>().terminalId.isNotEmpty()) {
        get<ParameterService>().downloadKeysAsync()
        get<CallHomeService>().startCallHomeTimer()
    }
}

fun KoinApplication.loadPosModules() {

    modules(module {
        single { ConfigService.getInstance(androidContext()) }
        single { PosDatabase.getInstance(androidContext()) }
        single { ParameterService.getInstance(androidContext()) }
        single { CallHomeService.getInstance(get(), get(), androidContext()) }
    })
}