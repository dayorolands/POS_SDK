package com.creditclub.core.data.api

import com.creditclub.core.data.model.StatesAndLgas
import com.creditclub.core.data.request.OfflineHLATaggingRequest
import com.creditclub.core.data.response.RequestStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/11/2019.
 * Appzone Ltd
 */
interface OfflineHlaTaggingService {

    @POST("api/OfflineHLATagging/PostOfflineTaggingData")
    suspend fun postOfflineTaggingData(@Body request: OfflineHLATaggingRequest): RequestStatus?

    @GET("api/OfflineHLATagging/GetStatesAndLGA")
    suspend fun getStatesAndLGA(): StatesAndLgas?
}