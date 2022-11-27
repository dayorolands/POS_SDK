package com.cluster.core.data.api

import com.cluster.core.data.model.NotificationReadResponse
import com.cluster.core.data.model.NotificationRequest
import com.cluster.core.data.model.NotificationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationService {
    @GET("api/Notification/MarkAsRead")
    suspend fun markAsRead(
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
        @Query("institutionCode") institutionCode: String?,
        @Query("reference") reference: String?
    ): NotificationReadResponse?

    @POST("api/Notification/GetNotifications")
    suspend fun getNotifications(@Body request: NotificationRequest): NotificationResponse?
}