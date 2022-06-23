package com.cluster.core.data.api

import com.cluster.core.data.model.*
import com.cluster.core.data.response.ApiResponse
import retrofit2.http.*

interface SubscriptionService {
    @GET("api/Subscription/GetActiveSubscription")
    suspend fun getActiveSubscription(
        @Query("institutionCode") institutionCode: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
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

    @GET("api/Subscription/GetMilestonesBySubscriptionID")
    suspend fun getMilestonesBySubscriptionId(
        @Query("subscriptionID") subscriptionId: Long,
    ): ApiResponse<List<SubscriptionMilestone>>

    @GET("api/Subscription/GetSubscriptionHistory")
    suspend fun getSubscriptionHistory(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("active") active: Boolean,
        @Query("startIndex") startIndex: Int,
        @Query("maxSize") maxSize: Int
    ): ApiResponse<ReportResult<Subscription>>

    @GET("api/Subscription/Fee")
    suspend fun getSubscriptionFee(
        @Query("planID") planId: Int,
        @Query("paymentType") paymentType: Int,
        @Query("institutionCode") institutionCode: String,
        @Query("phoneNumber") phoneNumber: String
    ): ApiResponse<Double>

    @POST("api/Subscription/Subscribe")
    suspend fun subscribe(@Body request: SubscriptionRequest): ApiResponse<String>

    @POST("api/Subscription/Extend")
    suspend fun extend(@Body request: SubscriptionRequest): ApiResponse<String>

    @POST("api/Subscription/Upgrade")
    suspend fun upgrade(@Body request: SubscriptionRequest): ApiResponse<String>

    @POST("api/Subscription/OptOutOfAutoRenew")
    suspend fun optOutOfAutoRenew(@Query("id") id : Long?): ApiResponse<String>

    @POST("api/Subscription/ChangePlan")
    suspend fun changeSubscriptionPlan(@Body request: ChangeSubscriptionRequest): ApiResponse<String>

    @POST("api/Subscription/Renew")
    suspend fun renewSubscription(@Body request: RenewSuscriptionRequest): ApiResponse<String>
}