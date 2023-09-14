package com.cluster.core.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.data.CoreDatabase
import com.cluster.core.data.api.AppConfig
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.logFunctionUsage
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject


open class CreditClubFragment : Fragment {
    constructor() : super()
    constructor(layout: Int) : super(layout)

    open val dialogProvider: DialogProvider by lazy {
        (requireActivity() as CreditClubActivity).dialogProvider
    }
    open val functionId: Int? = null

    open val localStorage: LocalStorage by inject()
    open val institutionConfig: InstitutionConfig by inject()
    open val appConfig: AppConfig by inject()
    open val coreDatabase: CoreDatabase by inject()

    open val mainScope = MainScope()
    open val ioScope by lazy { CoroutineScope(Dispatchers.IO) }

    var TextView.value: String
        get() = text.toString().trim { it <= ' ' }
        set(value) {
            text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        functionId?.also { id ->
            mainScope.launch {
                logFunctionUsage(id)
            }
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        ioScope.cancel()
        super.onDestroy()
    }

    fun startActivity(classToStart: Class<*>) {
        startActivity(Intent(requireActivity(), classToStart))
    }

    open fun onBackPressed(): Boolean {
        return false
    }
}