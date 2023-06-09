package com.cluster.pos.api

import com.cluster.pos.models.DisputedPosTransaction
import com.cluster.core.data.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ChargeBackService {

    @POST("api/ChargeBack/LogDispute")
    suspend fun logDispute(
        @Query("agentPin") agentPin: String,
        @Body request: DisputedPosTransaction,
    ): ApiResponse<String>?

    @POST("api/ChargeBack/GetDisputeDetailsByToken")
    suspend fun getDisputeDetailsByToken(
        @Query("institutionCode") institutionCode: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
        @Query("disputeToken") disputeToken: String?,
    ): ApiResponse<DisputedPosTransaction>?

    @POST("api/ChargeBack/GetDisputeTransactions")
    suspend fun getDisputeTransactions(
        @Query("institutionCode") institutionCode: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
        @Query("status") status: Int,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("startIndex") startIndex: Int,
        @Query("maxSize") maxSize: Int
    ): ApiResponse<List<DisputedPosTransaction>>?

    @POST("api/ChargeBack/ResolveDispute")
    suspend fun resolveDispute(
        @Query("institutionCode") institutionCode: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
        @Query("agentPin") agentPin: String,
        @Query("disputeToken") disputeToken: String?,
    ): ApiResponse<String>?
}