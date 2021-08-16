package com.creditclub.analytics.api

import com.creditclub.core.data.model.NetworkMeasurement
import com.creditclub.core.data.model.DeviceTransactionInformation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
interface MobileTrackingService {
    @POST("api/MobileTracking/SaveAgentMobileTrackingDetails")
    suspend fun saveAgentMobileTrackingDetails(@Body body: DeviceTransactionInformation): MobileTrackingResponse?

    @POST("api/MobileTracking/SaveAgentMobileTrackingDetails")
    suspend fun saveAgentMobileTrackingDetails(@Body body: List<DeviceTransactionInformation>): MobileTrackingResponse?

    @POST("api/MobileTracking/SaveAgentMobileTrackingDetails")
    suspend fun saveAgentMobileTrackingDetails(@Body body: RequestBody): MobileTrackingResponse?

    @POST("api/MobileTracking/MobileTracking")
    suspend fun logNetworkMetrics(@Body request: List<NetworkMeasurement>): MobileTrackingResponse?

    @Serializable
    data class MobileTrackingResponse(
        @SerialName("ReponseMessage")
        var responseMessage: String? = null,

        @SerialName("isSuccessful")
        var isSuccessful: Boolean = false
    )
}