package com.creditclub.core.data

import com.creditclub.core.data.api.*
import com.creditclub.core.util.delegates.service
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */
open class CreditClubMiddleWareAPI(okHttpClient: OkHttpClient, apiHost: String) {
    private val contentType = "application/json".toMediaType()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("${apiHost}/CreditClubMiddlewareAPI/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(
            Json(
                JsonConfiguration.Stable.copy(
                    isLenient = true,
                    ignoreUnknownKeys = true,
                    serializeSpecialFloatingPointValues = true,
                    useArrayPolymorphism = true
                )
            ).asConverterFactory(contentType)
        )
        .build()

    val staticService: StaticService by retrofit.service()
    val caseLogService: CaseLogService by retrofit.service()
    val reportService: ReportService by retrofit.service()
    val fundsTransferService: FundsTransferService by retrofit.service()
    val versionService: VersionService by retrofit.service()
    val collectionsService: CollectionsService by retrofit.service()
}