package com.creditclub.core.data.api

import com.creditclub.core.data.model.Notification
import com.creditclub.core.data.model.NotificationReadResponse
import com.creditclub.core.data.model.NotificationRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationService {
    @POST("api/Notification/MarkAsRead")
    suspend fun markAsRead(
        @Query("agentPhoneNumber") agentPhoneNumber: String,
        @Query("institutionCode") institutionCode: String?,
        @Query("reference") reference: String?
    ): NotificationReadResponse?

    @POST("api/Notification/GetNotifications")
    suspend fun getNotifications(@Body request: NotificationRequest): List<Notification>?
}