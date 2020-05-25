package com.creditclub.core.ui

import android.widget.TextView
import androidx.fragment.app.Fragment
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.TrackGPS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */
open class CreditClubFragment : Fragment() {
    open val activity get() = getActivity() as CreditClubActivity
    open val gps: TrackGPS by inject()
    open val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
    open val dialogProvider: DialogProvider by inject { parametersOf(activity) }

    open val mainScope by lazy { CoroutineScope(Dispatchers.Main) }
    open val ioScope by lazy { CoroutineScope(Dispatchers.IO) }
    open val defaultScope by lazy { CoroutineScope(Dispatchers.Default) }

    var TextView.value: String
        get() = text.toString().trim { it <= ' ' }
        set(value) {
            text = value
        }

    override fun onDestroy() {
        mainScope.cancel()
        ioScope.cancel()
        defaultScope.cancel()
        super.onDestroy()
    }
}