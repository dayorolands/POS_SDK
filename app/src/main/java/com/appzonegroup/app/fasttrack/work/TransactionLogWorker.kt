package com.appzonegroup.app.fasttrack.work

import android.content.Context
import android.os.Bundle
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.MIDDLEWARE_CLIENT
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.response.isSuccessful
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import retrofit2.create
import java.time.Instant
import java.time.temporal.ChronoUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/02/2020.
 * Appzone Ltd
 */
class TransactionLogWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) return@withContext Result.failure()

        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject(named(MIDDLEWARE_CLIENT))
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        val posDatabase: PosDatabase by inject()
        val appConfig: AppConfig by inject()

        val posTransactionDao = posDatabase.posTransactionDao()
        posTransactionDao.deleteSyncedBefore(Instant.now().minus(7, ChronoUnit.DAYS))
        val jobs = posTransactionDao.unSynced().map { receipt ->
            if (receipt.nodeName == "EPMS") receipt.nodeName = null
            async {
                val (response) = safeRunSuspend {
                    posApiService.transactionLog(
                        receipt,
                        "iRestrict ${appConfig.posNotificationToken}",
                        receipt.terminalId,
                    )
                }

                if (response.isSuccessful) {
                    posTransactionDao.save(receipt.apply { isSynced = true })
                }

                firebaseAnalytics.logEvent("transaction_log_attempt", Bundle().apply {
                    putString("terminal_id", receipt.terminalId)
                    putString("rrn", receipt.retrievalReferenceNumber)
                    putString("agent_code", receipt.agentCode)
                    putString("agent_phone", receipt.agentPhoneNumber)
                    putString("institution_code", receipt.institutionCode)
                    putBoolean("status", response.isSuccessful)
                })
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}