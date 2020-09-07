package com.creditclub.analytics.api

import com.creditclub.analytics.models.NetworkMeasurement
import com.creditclub.core.data.model.DeviceTransactionInformation
import com.creditclub.core.data.response.BackendResponse
import com.creditclub.core.data.response.MobileTrackingResponse
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

    @POST("api/MobileTracking/LogNetworkMetrics")
    suspend fun logNetworkMetrics(@Body request: NetworkMeasurement): BackendResponse?
}