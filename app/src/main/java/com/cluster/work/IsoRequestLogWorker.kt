package com.cluster.work

import android.content.Context
import android.os.Bundle
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.MIDDLEWARE_CLIENT
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import retrofit2.create

class IsoRequestLogWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) Result.failure()

        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject(named(MIDDLEWARE_CLIENT))
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        val posDatabase: PosDatabase by inject()
        val appConfig: AppConfig by inject()

        val isoRequestLogDao = posDatabase.isoRequestLogDao()
        val localStorage: LocalStorage by inject()
        val agentPhone = localStorage.agentPhone

        val jobs = isoRequestLogDao.all().map { requestLog ->
            async {
                val (response) = safeRunSuspend {
                    posApiService.logToGrafanaForPOSTransactions(
                        request = requestLog,
                        authToken = "iRestrict ${appConfig.posNotificationToken}",
                        terminalID = requestLog.terminalId,
                    )
                }

                if (response?.status == true) {
                    isoRequestLogDao.delete(requestLog)
                }

                firebaseAnalytics.logEvent("iso_log_attempt", Bundle().apply {
                    putString("terminal_id", requestLog.terminalId)
                    putString("rrn", requestLog.rrn)
                    putString("agent_code", requestLog.agentCode)
                    putString("agent_phone", agentPhone)
                    putString("institution_code", requestLog.institutionCode)
                    putBoolean("status", response?.status == true)
                })
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}