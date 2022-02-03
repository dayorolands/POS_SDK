package com.cluster.core.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.cluster.core.AppFunctions
import com.cluster.core.R
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.data.api.AppConfig
import com.cluster.core.data.api.VersionService
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.ui.widget.DialogListenerBlock
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.getMessage
import com.cluster.core.util.logFunctionUsage
import com.cluster.core.util.safeRunIO
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

abstract class CreditClubActivity : AppCompatActivity {

    constructor() : super()
    constructor(layout: Int) : super(layout)

    val dialogProvider: DialogProvider by inject { parametersOf(this) }
    val localStorage: LocalStorage by inject()
    val institutionConfig: InstitutionConfig by inject()
    val appConfig: AppConfig by inject()

    open val functionId: Int? = null

    val mainScope = MainScope()

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    protected lateinit var firebaseAnalytics: FirebaseAnalytics
        private set

    protected val firebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    inline var TextView.value: String
        get() = text.toString().trim { it <= ' ' }
        set(value) {
            text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val functionId = functionId
        if (functionId != null && AppFunctions[functionId] != null) {
            mainScope.launch {
                logFunctionUsage(functionId)
            }
        }

        val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        localStorage.agent?.let { agent ->
            firebaseAnalytics.setUserId(agent.agentCode)
            firebaseAnalytics.setUserProperty("agent_name", agent.agentName)
            firebaseAnalytics.setUserProperty("agent_code", agent.agentCode)
            firebaseAnalytics.setUserProperty("agent_phone", agent.phoneNumber)

            firebaseCrashlytics.setUserId(localStorage.agent?.agentCode ?: "guest")
        }
        firebaseAnalytics.setUserProperty("agent_institution", localStorage.institutionCode)
        firebaseCrashlytics.setCustomKey(
            "agent_institution",
            localStorage.institutionCode ?: "none"
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode == ConnectionResult.SUCCESS) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    localStorage.lastKnownLocation = "${location.latitude};${location.longitude}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    fun showNetworkError() = showNetworkError(null)

    fun showNetworkError(block: DialogListenerBlock<*>?) {
        dialogProvider.hideProgressBar()
        val message = getString(R.string.a_network_error_occurred)

        block ?: return dialogProvider.showError(message)
        dialogProvider.showError(message, block)
    }

    fun showInternalError() {
        dialogProvider.hideProgressBar()
        dialogProvider.showError(getString(R.string.an_error_occurred_please_try_again_later))
    }

    fun showError(exception: Exception, block: DialogListenerBlock<*>?) {
        dialogProvider.hideProgressBar()
        val message = exception.getMessage(this)

        block ?: return dialogProvider.showError(message)
        dialogProvider.showError(message, block)
    }

    fun showError(exception: Exception) = showError(exception, null)
}

suspend fun getLatestVersion(
    versionService: VersionService,
    appDataStorage: AppDataStorage,
    appConfig: AppConfig,
    localStorage: LocalStorage,
    deviceType: Int,
) = safeRunIO {
    val newVersion = versionService.getLatestVersionAndDownloadLink(
        appConfig.otaUpdateId,
        agentPhoneNumber = localStorage.agentPhone,
        institutionCode = localStorage.institutionCode,
        deviceType = deviceType,
    )
    if (newVersion != null) {
        val previousVersion = appDataStorage.latestVersion
        previousVersion?.run {
            if (version == newVersion.version) {
                newVersion.notifiedAt = previousVersion.notifiedAt
            }
        }
        appDataStorage.latestVersion = newVersion
    }

    newVersion
}