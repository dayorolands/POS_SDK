package com.creditclub.core.data.api

import com.creditclub.core.data.model.CommissionReport
import com.creditclub.core.type.ReportTypeField
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */
interface CommissionService {
    @GET("api/Commission/GetTransactions")
    suspend fun getTransactions(
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
        @Query("institutionCode") institutionCode: String?,
        @Query("transactionType") @ReportTypeField transactionType: Int,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("status") status: Int,
        @Query("startIndex") startIndex: Int,
        @Query("maxSize") maxSize: Int
    ): CommissionReport?
}