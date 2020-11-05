package com.appzonegroup.app.fasttrack.work

import android.content.Context
import android.os.Bundle
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.inject
import retrofit2.create

class IsoRequestLogWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) Result.failure()

        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        val posDatabase: PosDatabase by inject()
        val backendConfig: BackendConfig by inject()

        val isoRequestLogDao = posDatabase.isoRequestLogDao()
        val localStorage: LocalStorage by inject()
        val agentPhone = localStorage.agentPhone

        val jobs = isoRequestLogDao.all().map { requestLog ->
            if (requestLog.nodeName == "EPMS") requestLog.nodeName = null
            async {
                val (response) = safeRunSuspend {
                    posApiService.logToGrafanaForPOSTransactions(
                        requestLog,
                        "iRestrict ${backendConfig.posNotificationToken}",
                        requestLog.terminalId
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