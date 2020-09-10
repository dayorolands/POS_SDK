package com.appzonegroup.creditclub.pos

import android.app.Application
import com.creditclub.pos.PosProviders
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules

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
                loadPosModules()
                loadKoinModules(posManagerCompanion.module)
                application.startPosApp()
                return
            }
        }
    }
}
