package com.creditclub.core.ui

import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.creditclub.core.R
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.TrackGPS
import com.creditclub.core.util.getMessage
import com.creditclub.core.util.logFunctionUsage
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 7/23/2019.
 * Appzone Ltd
 */

abstract class CreditClubActivity : AppCompatActivity() {

    open val gps: TrackGPS by inject()
    open val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
    open val dialogProvider: DialogProvider by inject { parametersOf(this) }
    open val coreDatabase: CoreDatabase by inject()
    open val institutionConfig: IInstitutionConfig by inject()

    open val hasLogoutTimer = false
    open val functionId: Int? = null

    open val mainScope by lazy { CoroutineScope(Dispatchers.Main) }
    open val ioScope by lazy { CoroutineScope(Dispatchers.IO) }

    protected lateinit var firebaseAnalytics: FirebaseAnalytics
        private set

    var TextView.value: String
        get() = text.toString().trim { it <= ' ' }
        set(value) {
            text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (functionId != null) {
            mainScope.launch {
                logFunctionUsage(functionId!!)
            }
        }
    }

    fun showNetworkError() = showNetworkError<Nothing>(null)

    fun <T> showNetworkError(block: DialogListenerBlock<T>?) {
        dialogProvider.hideProgressBar()
        val message = getString(R.string.a_network_error_occurred)

        block ?: return dialogProvider.showError(message)
        dialogProvider.showError(message, block)
    }

    fun showInternalError() = showNetworkError<Nothing>(null)

    fun <T> showInternalError(block: DialogListenerBlock<T>?) {
        dialogProvider.hideProgressBar()
        val message = "An internal error occurred. Please try again later"

        block ?: return dialogProvider.showError(message)
        dialogProvider.showError(message, block)
    }

    fun <T> showError(exception: Exception, block: DialogListenerBlock<T>?) {
        dialogProvider.hideProgressBar()
        val message = exception.getMessage(this)

        block ?: return dialogProvider.showError(message)
        dialogProvider.showError(message, block)
    }

    fun showError(exception: Exception) = showError<Nothing>(exception, null)
}