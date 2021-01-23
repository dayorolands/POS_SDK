package com.appzonegroup.creditclub.pos

import android.app.Application
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.data.PosPreferences
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

object Platform : KoinComponent {

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
    single {
        val masterKeyAlias = MasterKey.Builder(androidContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            androidContext().applicationContext,
            "com.creditclub.pos.preferences",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        PosPreferences(prefs)
    }
}