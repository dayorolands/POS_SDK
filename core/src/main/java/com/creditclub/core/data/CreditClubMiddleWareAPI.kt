package com.creditclub.core.data

import com.creditclub.core.BuildConfig
import com.creditclub.core.data.api.*
import com.creditclub.core.util.delegates.service
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */
open class CreditClubMiddleWareAPI(okHttpClient: OkHttpClient) {
    private val contentType = MediaType.get("application/json")

    private val retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.API_HOST}/CreditClubMiddlewareAPI/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(Json.nonstrict.asConverterFactory(contentType))
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
}