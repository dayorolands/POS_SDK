package com.creditclub.analytics

import com.creditclub.analytics.models.NetworkMeasurement
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.safeRun
import io.objectbox.kotlin.boxFor
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class NetworkMetricsInterceptor : Interceptor, KoinComponent {
    private val localStorage: LocalStorage by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val metricsBox = AnalyticsObjectBox.boxStore.boxFor<NetworkMeasurement>()
        val request = chain.request()
        val networkMeasurement = NetworkMeasurement().apply {
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
            agentCode = localStorage.agent?.agentCode
            gpsCoordinates = localStorage.lastKnownLocation
            url = request.url().encodedPath()
            method = request.method()
            requestTime = Instant.now()
        }

        val (response, error) = safeRun { chain.proceed(request) }
        if (response != null) {
            networkMeasurement.apply {
                statusCode = response.code()
                responseTime = Instant.now()
            }
        } else networkMeasurement.statusCode = -1
        networkMeasurement.duration = networkMeasurement.run {
            Duration.between(requestTime, responseTime).toMillis()
        }
        metricsBox.put(networkMeasurement)
        if (error != null) throw error
        return chain.proceed(request)
    }
}