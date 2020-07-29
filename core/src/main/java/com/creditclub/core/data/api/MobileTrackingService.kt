package com.creditclub.core.data.api

import com.creditclub.core.data.model.DeviceTransactionInformation
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
}