package com.creditclub.core.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RequestFailureInterceptor : Interceptor {
    val json = Json(
        JsonConfiguration.Stable.copy(
            isLenient = true,
            ignoreUnknownKeys = true,
            serializeSpecialFloatingPointValues = true,
            useArrayPolymorphism = true
        )
    )

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code != 200) {
            val jsonPayload = response.body?.string() ?: return response
            val failureResponse = json.parse(FailureResponse.serializer(), jsonPayload)
            val message = failureResponse.message ?: return response
            throw RequestFailureException(message)
        }

        return response
    }

    @Serializable
    data class FailureResponse(@SerialName("Message") val message: String? = null)
}