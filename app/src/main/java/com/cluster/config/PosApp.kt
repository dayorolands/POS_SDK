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

fun Application.startPosApp() {
    encryptPosConfig()
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