package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.app.fasttrack.di.apiModule
import com.appzonegroup.app.fasttrack.di.configModule
import com.appzonegroup.app.fasttrack.di.dataModule
import com.appzonegroup.app.fasttrack.di.locationModule
import com.appzonegroup.creditclub.pos.posModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinComponent
import org.koin.dsl.koinApplication


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/02/2020.
 * Appzone Ltd
 */
abstract class BaseWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {

    private val koinApp = koinApplication {
        androidLogger()
        androidContext(applicationContext)

        modules(
            listOf(
                apiModule,
                locationModule,
                dataModule,
                configModule,
                posModule
            )
        )
    }

    override fun getKoin() = koinApp.koin
}