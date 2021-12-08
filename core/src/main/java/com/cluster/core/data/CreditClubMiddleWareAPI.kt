package com.cluster.core.data

import androidx.annotation.Keep
import com.cluster.core.util.delegates.defaultJson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

@Keep
data class HttpClientConfiguration(
    val retryEnabled: Boolean = true,
    val isBackground: Boolean = false,
    val traceEnabled: Boolean = true,
) {
    val qualifierName get() = "http.${hashCode()}"
}

val TRANSACTIONS_CLIENT = HttpClientConfiguration(retryEnabled = false).qualifierName
val MIDDLEWARE_CLIENT = HttpClientConfiguration().qualifierName

class CreditClubMiddleWareAPI(okHttpClient: OkHttpClient, apiHost: String) {
    @OptIn(ExperimentalSerializationApi::class)
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("${apiHost}/CreditClubMiddlewareAPI/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(defaultJson.asConverterFactory("application/json".toMediaType()))
        .build()
}