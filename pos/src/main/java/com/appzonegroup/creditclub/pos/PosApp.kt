package com.appzonegroup.creditclub.pos

import android.app.Application
import android.content.Intent
import androidx.work.*
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.service.SyncService
import com.appzonegroup.creditclub.pos.work.TransactionLogWorker
import com.creditclub.core.util.isMyServiceRunning
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

    if (!isMyServiceRunning(SyncService::class.java)) {
        startService(Intent(this, SyncService::class.java))
    }

    val workManager = WorkManager.getInstance(this)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val transactionLogRequest =
        PeriodicWorkRequestBuilder<TransactionLogWorker>(1, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

    workManager.enqueueUniquePeriodicWork(
        "TRANSACTION_LOG",
        ExistingPeriodicWorkPolicy.KEEP,
        transactionLogRequest
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