package com.cluster.config

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.cluster.BuildConfig
import com.cluster.R
import com.cluster.utility.extensions.registerPeriodicWorker
import com.cluster.work.*
import com.cluster.pos.Platform
import com.cluster.pos.service.ConfigService
import com.cluster.pos.util.SocketJob
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.data.prefs.moveTo
import com.cluster.pos.work.CallHomeWorker
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

    workManager.registerPeriodicWorker<CallHomeWorker>(constraints)

    workManager.registerPeriodicWorker<PosNotificationWorker>(
        uniqueWorkName = "APP_POS_NOTIFICATION",
        constraints = constraints,
    )

    // mPOS app updates will be handled by google play store
    if (Platform.deviceType != 2) {
        workManager.registerPeriodicWorker<AppUpdateWorker>(constraints)
    }
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
    val trustStore = KeyStore.getInstance("BKS")
    context.resources.openRawResource(R.raw.pos_trust_store).use { inputStream ->
        trustStore.load(inputStream, BuildConfig.KEYSTORE_PASS.toCharArray())
    }
    val tmf = TrustManagerFactory.getInstance("X509")
    tmf.init(trustStore)
    return tmf.trustManagers
}