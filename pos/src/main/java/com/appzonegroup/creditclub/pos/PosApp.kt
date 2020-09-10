package com.appzonegroup.creditclub.pos

import android.app.Application
import androidx.work.*
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.work.IsoRequestLogWorker
import com.appzonegroup.creditclub.pos.work.ReversalWorker
import com.appzonegroup.creditclub.pos.work.TransactionLogWorker
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
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

//    if (get<ConfigService>().terminalId.isNotEmpty()) {
//        GlobalScope.launch(Dispatchers.Main) {
//            safeRunIO { get<PosParameter>().downloadKeys() }
//        }
//        get<CallHomeService>().startCallHomeTimer()
//    }
}

fun loadPosModules() {

    loadKoinModules(module {
        single<PosConfig> { ConfigService(androidContext()) }
        single { PosDatabase.getInstance(androidContext()) }
        single<PosParameter>(override = true) {
            ParameterService(
                androidContext(),
                get<PosConfig>().remoteConnectionInfo
            )
        }
        single { CallHomeService() }
        single { IsoSocketHelper(get(), get()) }
    })
}