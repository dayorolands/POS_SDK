package com.creditclub.core.data.api

import com.creditclub.core.data.model.PosTransactionReport
import com.creditclub.core.data.model.TransactionReport
import com.creditclub.core.data.request.POSTransactionReportRequest
import com.creditclub.core.type.ReportTypeField
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/8/2019.
 * Appzone Ltd
 */

interface ReportService {
    @GET("api/Report/GetTransactions")
    suspend fun getTransactions(
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
        @Query("institutionCode") institutionCode: String?,
        @Query("transactionType") @ReportTypeField transactionType: Int,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("status") status: Int,
        @Query("startIndex") startIndex: Int,
        @Query("maxSize") maxSize: Int
    ): TransactionReport

    @POST("api/Report/POSTransactionsReport")
    suspend fun getPOSTransactions(@Body request: POSTransactionReportRequest): PosTransactionReport
}