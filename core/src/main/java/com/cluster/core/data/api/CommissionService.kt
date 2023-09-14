package com.cluster.core.data.api

import com.cluster.core.data.model.CommissionReport
import com.cluster.core.type.ReportTypeField
import retrofit2.http.GET
import retrofit2.http.Query


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