package com.creditclub.core

import android.app.Service
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.util.TrackGPS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.android.inject


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 30/09/2019.
 * Appzone Ltd
 */
abstract class CreditClubService : Service() {

    open val gps: TrackGPS by inject()
    open val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
    open val coreDatabase: CoreDatabase by inject()
    open val institutionConfig: IInstitutionConfig by inject()

    open val mainScope by lazy { CoroutineScope(Dispatchers.Main) }

    open val defaultScope by lazy { CoroutineScope(Dispatchers.Default) }
    open val ioScope by lazy { CoroutineScope(Dispatchers.IO) }
}