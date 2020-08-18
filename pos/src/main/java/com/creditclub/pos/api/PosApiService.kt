package com.creditclub.pos.api

import com.appzonegroup.creditclub.pos.models.IsoRequestLog
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.appzonegroup.creditclub.pos.models.PosTransaction
import com.creditclub.core.data.response.ApiResponse
import com.creditclub.core.data.response.PosNotificationResponse
import com.creditclub.core.data.response.RequestStatus
import com.creditclub.pos.model.BinRoutes
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

    // TODO: Migrate PosNotification from gson to kotlinx.serialization
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