package com.appzonegroup.creditclub.pos

import android.app.Application
import android.content.Intent
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.service.SyncService
import com.creditclub.core.util.isMyServiceRunning
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/11/2019.
 * Appzone Ltd
 */

fun Application.startPosApp() {

    if (!isMyServiceRunning(SyncService::class.java)) {
        startService(Intent(this, SyncService::class.java))
    }

    if (get<ConfigService>().terminalId.isNotEmpty()) {
        get<ParameterService>().downloadKeysAsync()
        get<CallHomeService>().startCallHomeTimer()
    }
}

fun loadPosModules() {

    loadKoinModules(module {
        single { ConfigService.getInstance(androidContext()) }
        single { PosDatabase.getInstance(androidContext()) }
        single { ParameterService.getInstance(androidContext()) }
        single { CallHomeService.getInstance(get(), get(), androidContext()) }
    })
}