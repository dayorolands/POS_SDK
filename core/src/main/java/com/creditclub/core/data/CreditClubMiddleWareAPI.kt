package com.creditclub.core.data

import com.creditclub.core.data.api.*
import com.creditclub.core.util.delegates.service
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
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

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = true
        encodeDefaults = true
    }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("${apiHost}/CreditClubMiddlewareAPI/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    val staticService: StaticService by retrofit.service()
    val caseLogService: CaseLogService by retrofit.service()
    val reportService: ReportService by retrofit.service()
    val versionService: VersionService by retrofit.service()
    val collectionsService: CollectionsService by retrofit.service()
}