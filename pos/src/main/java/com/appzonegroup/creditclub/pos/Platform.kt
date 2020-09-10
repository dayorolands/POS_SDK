package com.appzonegroup.creditclub.pos

import android.app.Application
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.helpers.IsoSocketHelper
import com.appzonegroup.creditclub.pos.service.CallHomeService
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import com.creditclub.pos.PosProviders
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/29/2019.
 * Appzone Ltd
 */
object Platform : KoinComponent {

    @JvmStatic
    var isPOS = false
        private set

    @JvmStatic
    val hasPrinter
        get() = isPOS

    fun test(application: Application) {
        for (posManagerCompanion in PosProviders.registered) {
            if (posManagerCompanion.isCompatible(application)) {
                isPOS = true
                posManagerCompanion.setup(application)
                loadKoinModules(module {
                    single<PosConfig> { ConfigService(androidContext()) }
                    single { PosDatabase.getInstance(androidContext()) }
                    single<PosParameter>(override = true) {
                        ParameterService(
                            androidContext(),
                            get<PosConfig>().remoteConnectionInfo
                        )
                    }
                    single { CallHomeService() }
                    single { IsoSocketHelper(get(), get()) }
                })
                loadKoinModules(posManagerCompanion.module)
                return
            }
        }
    }
}
