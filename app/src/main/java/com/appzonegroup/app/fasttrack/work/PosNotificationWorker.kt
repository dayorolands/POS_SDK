package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.prefs.LocalStorage
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
    CoroutineWorker(context, params), KoinComponent {

    private val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
    private val localStorage: LocalStorage by inject()
    private val configService: ConfigService by inject()
    private val posDatabase: PosDatabase by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (Platform.isPOS) return@withContext Result.failure()

        val gson = Gson()
        val posNotificationDao = posDatabase.posNotificationDao()

        val jobs = posNotificationDao.all().map { notification ->
            async {
                val requestBody = gson.toJson(notification).toRequestBody()

                val (response) = safeRunSuspend {
                    creditClubMiddleWareAPI.staticService.posCashOutNotification(
                        requestBody,
                        "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}",
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