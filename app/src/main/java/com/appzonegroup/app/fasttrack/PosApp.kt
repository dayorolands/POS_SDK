package com.appzonegroup.app.fasttrack

import android.app.Application
import android.os.Build
import androidx.work.*
import com.appzonegroup.app.fasttrack.work.IsoRequestLogWorker
import com.appzonegroup.app.fasttrack.work.ReversalWorker
import com.appzonegroup.app.fasttrack.work.TransactionLogWorker
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.creditclub.core.data.prefs.getEncryptedSharedPreferences
import com.creditclub.core.data.prefs.moveTo
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */

fun Application.startPosApp() {
    encryptPosConfig()

    val workManager = WorkManager.getInstance(this)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val transactionLogRequest =
        PeriodicWorkRequestBuilder<TransactionLogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

    workManager.enqueueUniquePeriodicWork(
        "APP_TRANSACTION_LOG",
        ExistingPeriodicWorkPolicy.KEEP,
        transactionLogRequest
    )

    workManager.enqueueUniquePeriodicWork(
        "APP_ISO_REQUEST_LOG",
        ExistingPeriodicWorkPolicy.KEEP,
        PeriodicWorkRequestBuilder<IsoRequestLogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
    )

    workManager.enqueueUniquePeriodicWork(
        "APP_REVERSAL",
        ExistingPeriodicWorkPolicy.KEEP,
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

private fun Application.encryptPosConfig() {
    val prefsName = "Config"
    val prefs = getSharedPreferences(
        prefsName,
        Application.MODE_PRIVATE
    )
    if (prefs.contains("TERMINAL_ID")) {
        prefs.moveTo(getEncryptedSharedPreferences(ConfigService.DEFAULT_FILE_NAME))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deleteSharedPreferences(prefsName)
        }
    }
}