package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appzonegroup.app.fasttrack.BuildConfig
import com.creditclub.analytics.api.MobileTrackingService
import com.appzonegroup.app.fasttrack.dataaccess.DeviceTransactionInformationDAO
import com.appzonegroup.app.fasttrack.model.DeviceTransactionInformation
import com.creditclub.analytics.AnalyticsObjectBox
import com.creditclub.analytics.models.NetworkMeasurement
import com.creditclub.core.data.NullOnEmptyConverterFactory
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.core.util.toRequestBody
import com.google.gson.Gson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


class MobileTrackingWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params), KoinComponent {

    private val metricsBox = AnalyticsObjectBox.boxStore.boxFor<NetworkMeasurement>()
    private val contentType = MediaType.get("application/json")
    private val okHttpClient = run {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)

        debugOnly {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
        }

        builder.build()
    }
    private val retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.API_HOST}/CreditClubMiddlewareAPI/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(
            Json(
                JsonConfiguration.Stable.copy(
                    isLenient = true,
                    ignoreUnknownKeys = true,
                    serializeSpecialFloatingPointValues = true,
                    useArrayPolymorphism = true
                )
            ).asConverterFactory(contentType)
        )
        .build()
    private val mobileTrackingService by retrofit.service(MobileTrackingService::class)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        logNetworkMetrics()
        logDeviceTransactionInfo()
        Result.success()
    }

    private suspend fun logNetworkMetrics() = coroutineScope {
        metricsBox.all.map { networkMeasurement ->
            async {
                val (response) = safeRunSuspend {
                    mobileTrackingService.logNetworkMetrics(networkMeasurement)
                }
                if (response?.isSuccessful == true) {
                    metricsBox.remove(networkMeasurement)
                }
            }
        }.awaitAll()
    }

    private suspend fun logDeviceTransactionInfo() {
        val gson = Gson()

        val localStorage: LocalStorage by inject()

        val transactionInformationDAO = DeviceTransactionInformationDAO(applicationContext)
        val allInfo = transactionInformationDAO.GetAll()

        if (allInfo.isEmpty()) return

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

        if (finishedTransactions.isEmpty()) return

        val dataToSend = gson.toJson(finishedTransactions)

        val (response) = safeRunSuspend {
            mobileTrackingService.saveAgentMobileTrackingDetails(dataToSend.toRequestBody())
        }

        if (response?.isSuccessful == true) {
            transactionInformationDAO.DeleteSentRecords(
                allInfo[0].id,
                allInfo[allInfo.size - 1].id
            )
        }

        transactionInformationDAO.close()
    }
}