package com.appzonegroup.app.fasttrack.work

import android.content.Context
import androidx.work.WorkerParameters
import com.appzonegroup.app.fasttrack.BuildConfig
import com.creditclub.analytics.AnalyticsObjectBox
import com.creditclub.analytics.api.MobileTrackingService
import com.creditclub.analytics.models.NetworkMeasurement
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.NullOnEmptyConverterFactory
import com.creditclub.core.data.model.DeviceTransactionInformation
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.safeRunSuspend
import com.google.gson.Gson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


class MobileTrackingWorker(context: Context, params: WorkerParameters) :
    BaseWorker(context, params) {

    private val metricsBox = AnalyticsObjectBox.boxStore.boxFor<NetworkMeasurement>()
    private val contentType = "application/json".toMediaType()
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
            Json {
                isLenient = true
                ignoreUnknownKeys = true
                allowSpecialFloatingPointValues = true
                useArrayPolymorphism = true
                encodeDefaults = true
            }.asConverterFactory(contentType)
        )
        .build()
    private val mobileTrackingService by retrofit.service<MobileTrackingService>()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        logNetworkMetrics()
        logDeviceTransactionInfo()
        Result.success()
    }

    private suspend fun logNetworkMetrics() = coroutineScope {
        metricsBox.all.asSequence().batch(5).map { networkMeasurements ->
            val networkMeasurementList = networkMeasurements.toList()
            async {
                val (response) = safeRunSuspend {
                    mobileTrackingService.logNetworkMetrics(networkMeasurementList)
                }
                if (response?.isSuccessful == true) {
                    metricsBox.remove(networkMeasurementList)
                }
            }
        }.toList().awaitAll()
    }

    private suspend fun logDeviceTransactionInfo() {
        val gson = Gson()
        val localStorage: LocalStorage by inject()

        val dao = CoreDatabase.getInstance(applicationContext).deviceTransactionInformationDao()
        val allInfo = dao.findAll()
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
            val request = dataToSend.toRequestBody(contentType)
            mobileTrackingService.saveAgentMobileTrackingDetails(request)
        }

        if (response?.isSuccessful == true) {
            dao.deleteRange(
                allInfo[0].id,
                allInfo[allInfo.size - 1].id
            )
        }
    }

    private fun <T> Sequence<T>.batch(n: Int): Sequence<List<T>> {
        return BatchingSequence(this, n)
    }

    private class BatchingSequence<T>(val source: Sequence<T>, val batchSize: Int) :
        Sequence<List<T>> {
        override fun iterator(): Iterator<List<T>> = object : AbstractIterator<List<T>>() {
            val iterate = if (batchSize > 0) source.iterator() else emptyList<T>().iterator()
            override fun computeNext() {
                if (iterate.hasNext()) setNext(iterate.asSequence().take(batchSize).toList())
                else done()
            }
        }
    }
}