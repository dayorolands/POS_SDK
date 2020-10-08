package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.create


class PosNotificationWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params), KoinComponent {

    private val configService: ConfigService by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (Platform.isPOS) return@withContext Result.failure()
        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
        val posDatabase: PosDatabase by inject()
        val backendConfig: BackendConfig by inject()
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        val posNotificationDao = posDatabase.posNotificationDao()

        val jobs = posNotificationDao.all().map { notification ->
            if (notification.nodeName == "EPMS") notification.nodeName = null
            async {
                val (response) = safeRunSuspend {
                    posApiService.posCashOutNotification(
                        notification,
                        "iRestrict ${backendConfig.posNotificationToken}",
                        notification.terminalId ?: configService.terminalId
                    )
                }

                if (!response?.billerReference.isNullOrBlank()) {
                    posNotificationDao.delete(notification)
                }
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}