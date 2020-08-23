package com.creditclub.core.data

import com.creditclub.core.data.api.*
import com.creditclub.core.util.delegates.service
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */
open class CreditClubMiddleWareAPI(okHttpClient: OkHttpClient, apiHost: String) {
    private val contentType = MediaType.get("application/json")

    val retrofit = Retrofit.Builder()
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

    val staticService by retrofit.service(StaticService::class)

    val mobileTrackingService by retrofit.service(MobileTrackingService::class)

    val billsPaymentService by retrofit.service(BillsPaymentService::class)

    val caseLogService by retrofit.service(CaseLogService::class)

    val commissionService by retrofit.service(CommissionService::class)

    val reportService by retrofit.service(ReportService::class)

    val fundsTransferService by retrofit.service(FundsTransferService::class)

    val onlineService by retrofit.service(BankOneService::class)

    val offlineHlaTaggingService by retrofit.service(OfflineHlaTaggingService::class)

    val versionService by retrofit.service(VersionService::class)

    val collectionsService by retrofit.service(CollectionsService::class)
}