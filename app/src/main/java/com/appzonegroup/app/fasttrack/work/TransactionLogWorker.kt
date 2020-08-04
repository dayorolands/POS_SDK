package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.PosTransaction
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.data.response.isSuccessful
import com.creditclub.core.util.safeRunSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.RequestBody
import org.koin.core.inject


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/02/2020.
 * Appzone Ltd
 */
class TransactionLogWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) return@withContext Result.failure()

        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
        val posDatabase: PosDatabase by inject()
        val backendConfig: BackendConfig by inject()

        val posTransactionDao = posDatabase.posTransactionDao()
        val mediaType = MediaType.parse("application/json")
        val serializer = PosTransaction.serializer()

        val jobs = posTransactionDao.all().map { receipt ->
            async {
                val requestBody = RequestBody.create(
                    mediaType,
                    Json(JsonConfiguration.Stable).stringify(serializer, receipt)
                )

                val (response) = safeRunSuspend {
                    creditClubMiddleWareAPI.staticService.transactionLog(
                        requestBody,
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