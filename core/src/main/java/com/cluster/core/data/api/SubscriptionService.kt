package com.cluster.core.data.api

import com.cluster.core.data.model.ReportResult
import com.cluster.core.data.model.Subscription
import com.cluster.core.data.model.SubscriptionPlan
import com.cluster.core.data.model.SubscriptionRequest
import com.cluster.core.data.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SubscriptionService {
    @GET("api/Subscription/GetActiveSubscription")
    suspend fun getActiveSubscription(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
    ): ApiResponse<Subscription>

    @GET("api/Subscription/GetRenewalInformationByPlanID")
    suspend fun getRenewalInformationByPlanId(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
        @Query("ActivePlanID") activePlanId: String,
    ): ApiResponse<SubscriptionPlan>

    @GET("api/Subscription/GetSubscriptionPlans")
    suspend fun getSubscriptionPlans(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
    ): ApiResponse<List<SubscriptionPlan>>

    @GET("api/Subscription/GetSubscriptionHistory")
    suspend fun getSubscriptionHistory(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("active") active: Boolean,
        @Query("startIndex") startIndex: Int,
        @Query("maxSize") maxSize: Int
    ): ApiResponse<ReportResult<SubscriptionPlan>>

    @POST("api/Subscription/Subscribe")
    suspend fun subscribe(@Body request: SubscriptionRequest): ApiResponse<String>

    @POST("api/Subscription/Extend")
    suspend fun extend(@Body request: SubscriptionRequest): ApiResponse<String>

    @POST("api/Subscription/Upgrade")
    suspend fun upgrade(@Body request: SubscriptionRequest): ApiResponse<String>
}