package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.data.response.isSuccessful
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.koin.core.inject
import retrofit2.create


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/02/2020.
 * Appzone Ltd
 */
class TransactionLogWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) return@withContext Result.failure()

        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        val posDatabase: PosDatabase by inject()
        val backendConfig: BackendConfig by inject()

        val posTransactionDao = posDatabase.posTransactionDao()

        val jobs = posTransactionDao.all().map { receipt ->
            if (receipt.nodeName == "EPMS") receipt.nodeName = null
            async {
                val (response) = safeRunSuspend {
                    posApiService.transactionLog(
                        receipt,
                        "iRestrict ${backendConfig.posNotificationToken}",
                        receipt.terminalId
                    )
                }

                if (response.isSuccessful) {
                    posTransactionDao.delete(receipt)
                }
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}