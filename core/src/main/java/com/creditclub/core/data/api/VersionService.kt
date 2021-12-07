package com.creditclub.core.data.api

import com.creditclub.core.data.model.AppVersion
import retrofit2.http.GET
import retrofit2.http.Query

interface VersionService {
    @GET("api/Version/GetLatestVersionAndDownloadLink")
    suspend fun getLatestVersionAndDownloadLink(
        @Query("appName") appName: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String?,
        @Query("institutionCode") institutionCode: String?,
        @Query("deviceType") deviceType: Int,
    ): AppVersion?
}