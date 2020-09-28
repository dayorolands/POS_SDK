package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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
            }
        }

        jobs.awaitAll()
        Result.success()
    }
}