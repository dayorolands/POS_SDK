package com.creditclub.core.data

import com.creditclub.core.data.api.CaseLogService
import com.creditclub.core.data.api.CollectionsService
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.core.util.delegates.service
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


const val TRANSACTIONS_CLIENT = "transactions"
const val MIDDLEWARE_CLIENT = "middleware"
const val BACKGROUND_CLIENT = "background"

class CreditClubMiddleWareAPI(okHttpClient: OkHttpClient, apiHost: String) {
    private val contentType = "application/json".toMediaType()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("${apiHost}/CreditClubMiddlewareAPI/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(defaultJson.asConverterFactory(contentType))
        .build()

    val staticService: StaticService by retrofit.service()
    val caseLogService: CaseLogService by retrofit.service()
    val collectionsService: CollectionsService by retrofit.service()
}