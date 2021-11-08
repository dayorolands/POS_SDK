package com.appzonegroup.app.fasttrack

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.appzonegroup.app.fasttrack.utility.extensions.registerPeriodicWorker
import com.appzonegroup.app.fasttrack.work.IsoRequestLogWorker
import com.appzonegroup.app.fasttrack.work.ReversalWorker
import com.appzonegroup.app.fasttrack.work.TransactionLogWorker
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.util.SocketJob
import com.creditclub.core.data.prefs.getEncryptedSharedPreferences
import com.creditclub.core.data.prefs.moveTo
import okio.use
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyStore
import java.security.Security
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */

fun Application.startPosApp() {
    SocketJob.setTrustManagers(getTrustManagers(this))
    encryptPosConfig()

    val workManager = WorkManager.getInstance(this)

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    workManager.cancelAllWork()

    workManager.registerPeriodicWorker<TransactionLogWorker>(
        uniqueWorkName = "APP_TRANSACTION_LOG",
        constraints = constraints,
    )

    workManager.registerPeriodicWorker<IsoRequestLogWorker>(
        uniqueWorkName = "APP_ISO_REQUEST_LOG",
        constraints = constraints,
    )

    workManager.registerPeriodicWorker<ReversalWorker>(
        uniqueWorkName = "APP_REVERSAL",
        constraints = constraints,
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

@Throws(Exception::class)
private fun getTrustManagers(context: Context): Array<TrustManager?> {
    Security.insertProviderAt(BouncyCastleProvider(), 1)
    val password = "cluster".toCharArray()
    val trustStore = KeyStore.getInstance("BKS")
    context.resources.openRawResource(R.raw.pos_trust_store).use { inputStream ->
        trustStore.load(inputStream, password)
    }
    val tmf = TrustManagerFactory.getInstance("X509")
    tmf.init(trustStore)
    return tmf.trustManagers
}