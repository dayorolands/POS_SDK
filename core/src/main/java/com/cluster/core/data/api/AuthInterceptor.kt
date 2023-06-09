package com.cluster.core.data.api

import com.cluster.core.data.prefs.AppDataStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val appDataStorage: AppDataStorage,
    private val appConfig: AppConfig,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequestBuilder = request.newBuilder().apply {
            addHeader("Device-Id", appDataStorage.deviceId ?: "unknown")
            addHeader("App-Name", appConfig.otaUpdateId)
            addHeader("App-Version", appConfig.versionName)
        }

        val newRequest = newRequestBuilder.build()
        return chain.proceed(newRequest)
    }
}