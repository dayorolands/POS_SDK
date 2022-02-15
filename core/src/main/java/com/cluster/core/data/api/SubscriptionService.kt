package com.cluster.core.data.api

import com.cluster.core.data.model.SubscriptionPlan
import com.cluster.core.data.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SubscriptionService {
    @GET("/api/Subscription/GetActiveSubscription")
    fun getActiveSubscription(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
    ): ApiResponse<SubscriptionPlan>

    @GET("/api/Subscription/GetRenewalInformationByPlanID")
    fun getRenewalInformationByPlanID(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
        @Query("ActivePlanID") activePlanId: String,
    )

    @GET("/api/Subscription/GetSubscriptionPlans")
    fun getSubscriptionPlans(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
    ): ApiResponse<List<SubscriptionPlan>>

    @GET("/api/Subscription/GetSubscriptionHistory")
    fun getSubscriptionHistory(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
    ): ApiResponse<List<SubscriptionPlan>>

    @POST("/api/Subscription/Subscribe")
    fun subscribe(@Body request: Any): ApiResponse<Any>

    @POST("/api/Subscription/Extend")
    fun extend(@Body request: Any): ApiResponse<Any>

    @POST("/api/Subscription/Upgrade")
    fun upgrade(@Body request: Any): ApiResponse<Any>

    @POST("/api/Subscription/AddSubscription")
    fun addSubscription(@Body request: Any): ApiResponse<Any>
}