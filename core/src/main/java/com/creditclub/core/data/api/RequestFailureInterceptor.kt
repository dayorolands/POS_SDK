package com.creditclub.core.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RequestFailureInterceptor : Interceptor {
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = true
        encodeDefaults = true
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val statusCode = response.code

        if (!response.isSuccessful) {
            val jsonPayload = response.body?.string() ?: return response
            val failureResponse = try {
                json.decodeFromString(FailureResponse.serializer(), jsonPayload)
            } catch (ex: SerializationException) {
                response.close()
                throw RequestFailureException(
                    "A server error has occurred. Please try again later",
                    statusCode
                )
            }
            val message = failureResponse.message ?: return response
            response.close()
            throw RequestFailureException(message, statusCode)
        }

        return response
    }

    @Serializable
    data class FailureResponse(@SerialName("Message") val message: String? = null)
}