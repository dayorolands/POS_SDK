package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.app.fasttrack.dataaccess.DeviceTransactionInformationDAO
import com.appzonegroup.app.fasttrack.model.DeviceTransactionInformation
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.MobileTrackingService
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.safeRunSuspend
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


class MobileTrackingWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val gson = Gson()

        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
        val mobileTrackingService by creditClubMiddleWareAPI.retrofit.service<MobileTrackingService>()
        val localStorage: LocalStorage by inject()

        val transactionInformationDAO = DeviceTransactionInformationDAO(applicationContext)
        val allInfo = transactionInformationDAO.GetAll()

        if (allInfo.isEmpty()) return@withContext Result.success()

        val finishedTransactions = ArrayList<DeviceTransactionInformation>()
        val sessionId = localStorage.authResponse?.sessionId ?: "nothing"

        for (information in allInfo) {
            if (information.sessionID != sessionId) {
                val sum =
                    information.successCount + information.errorResponse + information.noInternet + information.noInternet
                if (information.requestCount > sum) {
                    information.noInternet = information.noInternet + 1
                }
                finishedTransactions.add(information)
            }
        }

        if (finishedTransactions.isEmpty()) return@withContext Result.success()

        val dataToSend = gson.toJson(finishedTransactions)

        val mediaType = "application/json".toMediaTypeOrNull()
        val (response) = safeRunSuspend {
            mobileTrackingService.saveAgentMobileTrackingDetails(dataToSend.toRequestBody(mediaType))
        }

        if (response?.isSuccessful == true) {
            transactionInformationDAO.DeleteSentRecords(
                allInfo[0].id,
                allInfo[allInfo.size - 1].id
            )
        }

        transactionInformationDAO.close()
        Result.success()
    }
}