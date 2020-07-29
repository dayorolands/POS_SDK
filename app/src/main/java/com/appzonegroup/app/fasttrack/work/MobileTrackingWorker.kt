package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.app.fasttrack.dataaccess.DeviceTransactionInformationDAO
import com.appzonegroup.app.fasttrack.model.DeviceTransactionInformation
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.core.util.toRequestBody
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


class MobileTrackingWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {

    private val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
    private val localStorage: LocalStorage by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val gson = Gson()

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

        val (response) = safeRunSuspend {
            creditClubMiddleWareAPI
                .mobileTrackingService
                .saveAgentMobileTrackingDetails(dataToSend.toRequestBody())
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