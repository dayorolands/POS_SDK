package com.cluster.core.data.api

import com.cluster.core.data.model.PosTransactionReport
import com.cluster.core.data.model.TransactionReport
import com.cluster.core.data.request.POSTransactionReportRequest
import com.cluster.core.data.request.PWTTransactionReportRequest
import com.cluster.core.type.ReportTypeField
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

    @POST("api/Report/POSCashOutTransactionsReport")
    suspend fun getPOSTransactions(@Body request: POSTransactionReportRequest): PosTransactionReport

    @POST("api/Report/PayWithTransferTransactionsReport")
    suspend fun getPWTTransactions(
        @Body request: PWTTransactionReportRequest
    ) : PosTransactionReport
}