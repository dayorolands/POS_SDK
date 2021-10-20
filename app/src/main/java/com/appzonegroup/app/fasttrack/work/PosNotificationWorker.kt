package com.appzonegroup.app.fasttrack.work

import android.content.Context
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.pos.api.PosApiService
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.create


class PosNotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val localStorage: LocalStorage,
    private val posDatabase: PosDatabase,
    private val appConfig: AppConfig,
    private val configService: ConfigService,
    private val creditClubMiddleWareAPI: CreditClubMiddleWareAPI,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        val posNotificationDao = posDatabase.posNotificationDao()
        val agentCode = localStorage.agent?.agentCode
        val agentPhone = localStorage.agentPhone
        val institutionCode = localStorage.institutionCode
        val firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        val jobs = posNotificationDao.all().map { notification ->
            if (notification.nodeName == "EPMS") notification.nodeName = null
            async {
                val (response) = safeRunSuspend {
                    posApiService.posCashOutNotification(
                        notification,
                        "iRestrict ${appConfig.posNotificationToken}",
                        notification.terminalId ?: configService.terminalId
                    )
                }

                if (!response?.billerReference.isNullOrBlank()) {
                    posNotificationDao.delete(notification)
                }

                firebaseAnalytics.logEvent("pos_notification_attempt", Bundle().apply {
                    putString("terminal_id", notification.terminalId)
                    putString("rrn", notification.retrievalReferenceNumber)
                    putString("agent_code", agentCode)
                    putString("agent_phone", agentPhone)
                    putString("institution_code", institutionCode)
                    putBoolean("status", !response?.billerReference.isNullOrBlank())
                })
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}