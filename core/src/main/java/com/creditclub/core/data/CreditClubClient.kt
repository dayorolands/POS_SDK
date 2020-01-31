package com.creditclub.core.data

import com.creditclub.core.BuildConfig
import com.creditclub.core.data.api.*
import com.creditclub.core.util.delegates.service
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */
open class CreditClubClient {
    private val certificatePinner = CertificatePinner.Builder()
        .add("api.mybankone.com", "sha256/goId03pe7sxzYmTdNcd1vI+psOY/FX5YGYjkPeioB0w=")
        .build()

    private val okHttpClient = OkHttpClient().newBuilder()
        .certificatePinner(certificatePinner)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("${BuildConfig.HOST}/CreditClubClient/HttpJavaClient/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val bankOneService by retrofit.service(BankOneService::class)
}