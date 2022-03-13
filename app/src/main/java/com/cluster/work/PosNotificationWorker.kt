package com.cluster.work

import android.content.Context
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cluster.core.data.CreditClubMiddleWareAPI
import com.cluster.core.data.api.AppConfig
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.safeRunSuspend
import com.cluster.core.util.toInstant
import com.cluster.pos.PosConfig
import com.cluster.pos.api.PosApiService
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.model.nibssNodeNameSet
import com.cluster.pos.models.PosNotification
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import retrofit2.create
import java.time.Instant


class PosNotificationWorker(
    context: Context,
    params: WorkerParameters,
    private val localStorage: LocalStorage,
    private val posDatabase: PosDatabase,
    private val appConfig: AppConfig,
    private val posConfig: PosConfig,
    private val creditClubMiddleWareAPI: CreditClubMiddleWareAPI,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        val posNotificationDao = posDatabase.posNotificationDao()
        val agentCode = localStorage.agent?.agentCode
        val agentPhone = localStorage.agentPhone
        val institutionCode = localStorage.institutionCode
        val firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        val threeMinsAgo = Instant.now().minusSeconds(3 * 60)
        val posNotifications = posNotificationDao.all().filter {
            val instant = it.paymentDate!!.toInstant(PosNotification.PAYMENT_DATE_PATTERN)
            instant.isBefore(threeMinsAgo)
        }
        val nibssNodeNames = nibssNodeNameSet()
        val jobs = posNotifications.map { notification ->
            if (nibssNodeNames.contains(notification.nodeName)) {
                notification.nodeName = null
            }
            async {
                val (response) = safeRunSuspend {
                    posApiService.posCashOutNotification(
                        notification,
                        "iRestrict ${appConfig.posNotificationToken}",
                        notification.terminalId ?: posConfig.terminalId
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