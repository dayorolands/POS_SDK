package com.creditclub.analytics

import com.creditclub.analytics.models.NetworkMeasurement
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.data.api.RequestFailureException
import com.creditclub.core.data.prefs.LocalStorage
import io.objectbox.kotlin.boxFor
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.Duration
import java.time.Instant

class NetworkMetricsInterceptor : Interceptor, KoinComponent {
    private val localStorage: LocalStorage by inject()
    private val backendConfig: BackendConfig by inject()
    private val metricsBox by lazy { AnalyticsObjectBox.boxStore.boxFor<NetworkMeasurement>() }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val flowName = url.queryParameter("FlowName")
        val flowId = url.queryParameter("FlowID")
        val networkMeasurement = NetworkMeasurement(
            institutionCode = localStorage.institutionCode,
            agentPhoneNumber = localStorage.agentPhone,
            agentCode = localStorage.agent?.agentCode,
            gpsCoordinates = localStorage.lastKnownLocation,
            url = url.encodedPath,
            method = request.method,
            requestTime = Instant.now(),
            appName = backendConfig.appName,
            appVersion = backendConfig.versionName,
            flowName = flowName,
            flowId = flowId,
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