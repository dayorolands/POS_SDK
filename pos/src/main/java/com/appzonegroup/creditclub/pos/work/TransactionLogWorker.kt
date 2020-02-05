package com.appzonegroup.creditclub.pos.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.models.Receipt
import com.creditclub.core.util.safeRunSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.RequestBody


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 03/02/2020.
 * Appzone Ltd
 */
class TransactionLogWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) Result.failure()

        val receiptDao = posDatabase.receiptDao()
        val mediaType = MediaType.parse("application/json")
        val serializer = Receipt.serializer()

        val jobs = receiptDao.all().map { receipt ->
            async {
                val requestBody = RequestBody.create(
                    mediaType,
                    Json.stringify(serializer, receipt)
                )

                val (response) = safeRunSuspend {
                    creditClubMiddleWareAPI.staticService.transactionLog(
                        requestBody,
                        "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}",
                        receipt.terminalId
                    )
                }

                if (response?.status == true) {
                    receiptDao.delete(receipt)
                }
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}