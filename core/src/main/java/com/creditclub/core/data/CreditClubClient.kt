package com.creditclub.core.data

import com.creditclub.core.data.api.BankOneService
import com.creditclub.core.util.delegates.service
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */
open class CreditClubClient(okHttpClient: OkHttpClient, baseUrl: String) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("${baseUrl}/CreditClubClient/HttpJavaClient/")
        .client(okHttpClient)
        .addConverterFactory(NullOnEmptyConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val bankOneService by retrofit.service<BankOneService>()
}