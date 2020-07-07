package com.creditclub.analytics

import com.creditclub.analytics.models.NetworkMeasurement
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.util.safeRun
import io.objectbox.kotlin.boxFor
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.Instant

class NetworkMetricsInterceptor : Interceptor, KoinComponent {
    private val localStorage: LocalStorage by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val metricsBox = ObjectBox.boxStore.boxFor<NetworkMeasurement>()
        val request = chain.request()
        val url = request.url()
        val networkMeasurement = NetworkMeasurement().apply {
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
            agentCode = localStorage.agent?.agentCode
            host = url.host()
            path = url.encodedPath()
            scheme = url.scheme()
            method = request.method()
        }

        val (response, error) = safeRun { chain.proceed(request) }
        if (response != null) {
            networkMeasurement.apply {
                statusCode = response.code()
                responseTime = Instant.now()
            }
        }
        metricsBox.put(networkMeasurement)

        if (error != null) throw error
        return chain.proceed(request)
    }
}