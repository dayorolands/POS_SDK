package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.core.util.toRequestBody
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject


class PosNotificationWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val gson = Gson()

        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
        val posDatabase: PosDatabase by inject()
        val backendConfig: BackendConfig by inject()
        val configService = ConfigService.getInstance(applicationContext)
        val posNotificationDao = posDatabase.posNotificationDao()

        val jobs = posNotificationDao.all().map { notification ->
            async {
                val requestBody = gson.toJson(notification).toRequestBody()

                val (response) = safeRunSuspend {
                    creditClubMiddleWareAPI.staticService.posCashOutNotification(
                        requestBody,
                        "iRestrict ${backendConfig.posNotificationToken}",
                        configService.terminalId
                    )
                }

                if (response?.isSuccessFul == true) {
                    posNotificationDao.delete(notification)
                }
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}