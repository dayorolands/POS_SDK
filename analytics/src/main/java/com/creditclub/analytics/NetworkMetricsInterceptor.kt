package com.creditclub.analytics

import com.creditclub.core.data.ClusterObjectBox
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.api.RequestFailureException
import com.creditclub.core.data.model.NetworkMeasurement
import com.creditclub.core.data.prefs.AppDataStorage
import com.creditclub.core.data.prefs.LocalStorage
import io.objectbox.kotlin.boxFor
import okhttp3.Interceptor
import okhttp3.Response
import java.time.Duration
import java.time.Instant

class NetworkMetricsInterceptor(
    private val appConfig: AppConfig,
    private val appDataStorage: AppDataStorage,
    private val localStorage: LocalStorage,
    private val clusterObjectBox: ClusterObjectBox,
) : Interceptor {
    private val metricsBox by lazy { clusterObjectBox.boxStore.boxFor<NetworkMeasurement>() }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val flowName = url.queryParameter("FlowName")
        val flowId = url.queryParameter("FlowID")
        val newUrl = url.newBuilder()
            .removeAllQueryParameters("FlowName")
            .removeAllQueryParameters("FlowID")
            .removeAllQueryParameters("agentPIN")
            .build()
        val agent = localStorage.agent

        val networkMeasurement = NetworkMeasurement(
            institutionCode = localStorage.institutionCode,
            agentPhoneNumber = localStorage.agentPhone,
            agentCode = agent?.agentCode,
            gpsCoordinates = localStorage.lastKnownLocation,
            url = newUrl.toString().replace(',', '.'),
            method = request.method,
            requestTime = Instant.now(),
            appName = appConfig.appName,
            appVersion = appConfig.versionName,
            flowName = flowName,
            flowId = flowId,
            networkState = appDataStorage.networkState,
            networkCarrier = appDataStorage.networkCarrier,
            terminalId = agent?.terminalID,
        )

        var response: Response? = null
        var error: Throwable? = null
        try {
            response = chain.proceed(request)
        } catch (e: RequestFailureException) {
            error = e
            if (e.httpStatusCode != null) {
                networkMeasurement.statusCode = e.httpStatusCode!!
                networkMeasurement.responseTime = Instant.now()
            }
        } catch (t: Throwable) {
            error = t
        }
        if (response != null) {
            networkMeasurement.apply {
                statusCode = response.code
                responseTime = Instant.now()
            }
        } else networkMeasurement.statusCode = -1
        networkMeasurement.duration = networkMeasurement.run {
            Duration.between(requestTime, responseTime ?: Instant.now()).toMillis()
        }
        if (error != null) {
            networkMeasurement.message = error.message
            metricsBox.put(networkMeasurement)
            throw error
        }
        metricsBox.put(networkMeasurement)
        return response!!
    }
}