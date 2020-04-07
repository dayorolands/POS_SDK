package com.appzonegroup.creditclub.pos.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.BuildConfig
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.core.util.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class IsoRequestLogWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!Platform.isPOS) Result.failure()

        val isoRequestLogDao = posDatabase.isoRequestLogDao()
        val serializer = IsoRequestLog.serializer()

        val jobs = isoRequestLogDao.all().map { requestLog ->
            async {
                val requestBody = Json.stringify(serializer, requestLog).toRequestBody()

                val (response) = safeRunSuspend {
                    creditClubMiddleWareAPI.staticService.logToGrafanaForPOSTransactions(
                        requestBody,
                        "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}",
                        requestLog.terminalId
                    )
                }

                if (response?.status == true) {
                    isoRequestLogDao.delete(requestLog)
                }
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}