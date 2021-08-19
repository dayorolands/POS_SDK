package com.creditclub.core.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.creditclub.core.config.InstitutionConfig
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.MIDDLEWARE_CLIENT
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.logFunctionUsage
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named


/**
 * Created by Emmanuel Nosakhare <enosakhare@app zonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */
open class CreditClubFragment : Fragment {
    constructor() : super()
    constructor(layout: Int) : super(layout)

    open val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject(named(MIDDLEWARE_CLIENT))
    open val dialogProvider: DialogProvider by inject { parametersOf(activity) }
    open val functionId: Int? = null

    open val localStorage: LocalStorage by inject()
    open val appDataStorage: AppDataStorage by inject()
    open val institutionConfig: InstitutionConfig by inject()
    open val appConfig: AppConfig by inject()
    open val coreDatabase: CoreDatabase by inject()

    open val mainScope by lazy { CoroutineScope(Dispatchers.Main) }
    open val ioScope by lazy { CoroutineScope(Dispatchers.IO) }
    open val defaultScope by lazy { CoroutineScope(Dispatchers.Default) }

    val firebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    var TextView.value: String
        get() = text.toString().trim { it <= ' ' }
        set(value) {
            text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        functionId?.also { id ->
            mainScope.launch {
                requireContext().logFunctionUsage(id)
            }
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        ioScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
    }

    fun startActivity(classToStart: Class<*>) {
        startActivity(Intent(requireActivity(), classToStart))
    }

    open fun onBackPressed(): Boolean {
        return false
    }
}