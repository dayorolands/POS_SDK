package com.creditclub.core.data.api

import com.creditclub.core.data.model.CoraPayReference
import com.creditclub.core.data.model.CoraPayTransactionStatus
import com.creditclub.core.data.request.CoralPayReferenceRequest
import com.creditclub.core.data.response.ApiResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CoralPayService {
    @POST("CoralpayCashout/GenerateReference")
    suspend fun generateReference(
        @Query("request") request: CoralPayReferenceRequest,
    ): ApiResponse<CoraPayReference>?

    @GET("CoralpayCashout/GetTransactionStatus")
    suspend fun getTransactionStatus(
        @Query("RequestReference") requestReference: String,
        @Query("InstitutionCode") institutionCode: String?,
    ): ApiResponse<CoraPayTransactionStatus>?
}