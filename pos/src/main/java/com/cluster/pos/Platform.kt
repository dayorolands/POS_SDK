package com.cluster.pos

import android.app.Application
import androidx.work.WorkerParameters
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.data.PosPreferences
import com.cluster.pos.helpers.IsoSocketHelper
import com.cluster.pos.service.CallHomeService
import com.cluster.pos.service.ConfigService
import com.cluster.core.data.prefs.getEncryptedSharedPreferences
import com.cluster.core.util.readRawJsonFile
import com.cluster.pos.*
import com.cluster.pos.model.PosTenant
import com.cluster.pos.work.CallHomeWorker
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
        PosPreferences(androidContext().getEncryptedSharedPreferences("pos_preferences_0"))
    }
    worker { (workerParams: WorkerParameters) ->
        CallHomeWorker(
            context = androidContext(),
            params = workerParams,
            socketHelper = get(),
        )
    }
}
