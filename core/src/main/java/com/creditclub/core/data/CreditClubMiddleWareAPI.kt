package com.creditclub.core.data

import com.creditclub.core.util.delegates.defaultJson
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


const val TRANSACTIONS_CLIENT = "transactions"
const val MIDDLEWARE_CLIENT = "middleware"
const val BACKGROUND_CLIENT = "background"

class CreditClubMiddleWareAPI(okHttpClient: OkHttpClient, apiHost: String) {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("${apiHost}/CreditClubMiddlewareAPI/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(defaultJson.asConverterFactory("application/json".toMediaType()))
        .build()
}