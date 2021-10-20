package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatDelegate
import com.appzonegroup.app.fasttrack.di.*
import com.appzonegroup.app.fasttrack.model.online.AuthResponse
import com.appzonegroup.app.fasttrack.utility.extensions.registerWorkers
import com.appzonegroup.app.fasttrack.utility.registerAppFunctions
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.R
import com.creditclub.core.data.clusterObjectBoxModule
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.prefs.moveTo
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

class BankOneApplication : Application() {
    private val appDataStorage: AppDataStorage by inject()
    private val localStorage: LocalStorage by inject()

    val authResponse: AuthResponse by lazy {
        val phoneNumber = "234${localStorage.agentPhone?.substring(1)}"
        val newAuth = localStorage.authResponse
            ?: return@lazy AuthResponse(
                phoneNumber,
                localStorage.getString("AGENT_CODE") ?: "",
                localStorage.getString("AGENT_CODE") ?: ""
            )

        return@lazy AuthResponse(
            newAuth.phoneNumber ?: phoneNumber,
            newAuth.sessionId ?: localStorage.getString("AGENT_CODE") ?: "",
            newAuth.activationCode ?: localStorage.getString("AGENT_CODE") ?: ""
        )
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Picasso.setSingletonInstance(
            Picasso.Builder(this).loggingEnabled(BuildConfig.DEBUG).build()
        )

        startKoin {
            androidLogger()
            androidContext(this@BankOneApplication)
            androidFileProperties()
            fragmentFactory()
            workManagerFactory()

            modules(
                listOf(
                    clusterObjectBoxModule,
                    apiModule,
                    dataModule,
                    uiModule,
                    configModule,
                    sharingModule,
                    workerModule,
                )
            )
        }

        encryptAgentInfo()
        observeNetworkState()
        @SuppressLint("HardwareIds")
        appDataStorage.deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
        registerAppFunctions()
        Platform.test(this)

        if (Platform.isPOS) {
            loadKoinModules(posWorkerModule)
            startPosApp()
        }
        registerWorkers()
    }

    private fun encryptAgentInfo() {
        val prefsName = getString(R.string.DATA_SOURCE)
        val prefs = getSharedPreferences(
            prefsName,
            MODE_PRIVATE
        )
        if (prefs.contains(LocalStorage.AGENT_INFO)) {
            prefs.moveTo(get<LocalStorage>())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                deleteSharedPreferences(prefsName)
            }
        }
    }

    private fun observeNetworkState() {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        appDataStorage.networkCarrier = manager.networkOperatorName
        cm.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                appDataStorage.networkState = "AVAILABLE"
                appDataStorage.networkCarrier = manager.networkOperatorName
            }

            override fun onUnavailable() {
                super.onUnavailable()
                appDataStorage.networkState = "UNAVAILABLE"
                appDataStorage.networkCarrier = manager.networkOperatorName
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                appDataStorage.networkState = "LOST"
                appDataStorage.networkCarrier = manager.networkOperatorName
            }
        })
    }
}
