package com.appzonegroup.creditclub.pos

import android.app.Application
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.data.PosPreferences
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.creditclub.core.data.prefs.getEncryptedSharedPreferences
import com.creditclub.core.util.readRawJsonFile
import com.creditclub.pos.*
import com.creditclub.pos.model.PosTenant
import com.creditclub.pos.work.CallHomeWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.context.loadKoinModules
import org.koin.dsl.bind
import org.koin.dsl.module

object Platform {

    @JvmStatic
    var isPOS = false
        private set

    @JvmStatic
    var posId = ""
        private set

    @JvmStatic
    var deviceType = 2
        private set

    @JvmStatic
    val hasPrinter
        get() = isPOS

    fun test(application: Application) {
        for (posManagerCompanion in PosProviders.registered) {
            if (posManagerCompanion.isCompatible(application)) {
                isPOS = true
                posId = posManagerCompanion.id
                deviceType = posManagerCompanion.deviceType
                posManagerCompanion.setup(application)
                loadKoinModules(posModule)
                loadKoinModules(posManagerCompanion.module)
                return
            }
        }
    }
}

val posModule = module {
    single {
        androidContext().readRawJsonFile(R.raw.pos_tenant, PosTenant.serializer())
    }
    single<RemoteConnectionInfo> {
        val infoList = get<PosTenant>().infoList
        if (infoList.isEmpty()) InvalidRemoteConnectionInfo
        else infoList[0]
    }
    single { ConfigService(androidContext()) }.bind(PosConfig::class)
    single { PosDatabase.getInstance(androidContext()) }
    single {
        get<PosConfig>().remoteConnectionInfo.getParameter(androidContext())
    }
    single { CallHomeService(androidContext()) }
    single {
        IsoSocketHelper(
            config = get(),
            parameters = get(),
        )
    }
    single {
        PosPreferences(androidContext().getEncryptedSharedPreferences("com.creditclub.pos.preferences"))
    }
    worker { (workerParams: WorkerParameters) ->
        CallHomeWorker(
            context = androidContext(),
            params = workerParams,
            socketHelper = get(),
        )
    }
}
