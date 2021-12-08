package com.cluster.pos.api

import com.cluster.pos.models.IsoRequestLog
import com.cluster.pos.models.PosNotification
import com.cluster.pos.models.PosTransaction
import com.cluster.core.data.response.ApiResponse
import com.cluster.core.data.response.PosNotificationResponse
import com.cluster.core.data.response.RequestStatus
import com.cluster.pos.model.BinRoutes
import retrofit2.http.*

interface PosApiService {
    @GET("CreditClubStatic/GetBINRoutes")
    suspend fun getBinRoutes(
        @Query("InstitutionCode") institutionCode: String?,
        @Query("AgentPhoneNumber") agentPhoneNumber: String?
    ): ApiResponse<List<BinRoutes>>

    @POST("CreditClubStatic/TransactionLog")
    suspend fun transactionLog(
        @Body request: PosTransaction,
        @Header("Authorization") authToken: String,
        @Header("TerminalID") terminalID: String?
    ): ApiResponse<String>?

    @POST("CreditClubStatic/POSCashOutNotification")
    suspend fun posCashOutNotification(
        @Body request: PosNotification,
        @Header("Authorization") authToken: String,
        @Header("TerminalID") terminalID: String
    ): PosNotificationResponse?

    @POST("CreditClubStatic/LogToGrafanaForPOSTransactions")
    suspend fun logToGrafanaForPOSTransactions(
        @Body request: IsoRequestLog,
        @Header("Authorization") authToken: String,
        @Header("TerminalID") terminalID: String
    ): RequestStatus?
}