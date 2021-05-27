package com.creditclub.core.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.creditclub.core.AppFunctions
import com.creditclub.core.R
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.getMessage
import com.creditclub.core.util.logFunctionUsage
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

abstract class CreditClubActivity : AppCompatActivity {

    constructor() : super()
    constructor(layout: Int) : super(layout)

    open val gps: TrackGPS by inject()
    open val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
    open val dialogProvider: DialogProvider by inject { parametersOf(this) }
    open val coreDatabase: CoreDatabase by inject()
    open val localStorage: LocalStorage by inject()
    open val institutionConfig: IInstitutionConfig by inject()
    open val appConfig: AppConfig by inject()

    open val hasLogoutTimer = false
    open val functionId: Int? = null

    open val mainScope by lazy { CoroutineScope(Dispatchers.Main) }
    open val ioScope by lazy { CoroutineScope(Dispatchers.IO) }

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

        localStorage.agent?.let { agent ->
            firebaseAnalytics.setUserId(agent.agentCode)
            firebaseAnalytics.setUserProperty("agent_name", agent.agentName)
            firebaseAnalytics.setUserProperty("agent_code", agent.agentCode)
            firebaseAnalytics.setUserProperty("agent_phone", agent.phoneNumber)
        }

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
        mainScope.cancel()
        ioScope.cancel()
        super.onDestroy()
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