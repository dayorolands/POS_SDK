package com.cluster.core.data.api

import com.cluster.core.data.model.*
import com.cluster.core.data.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface PayWithTransferService {

    @POST("api/PayWithTransfer/InitiatePayment")
    suspend fun initiatePayment(
        @Body request: InitiatePaymentRequest
    ) : ApiResponse<InitiatePayment>

    @GET("api/PayWithTransfer/GetFee")
    suspend fun getAmountFee(
        @Query("institutionCode") institutionCode: String,
        @Query("agentPhoneNumber") phoneNumber : String,
        @Query("amount") amount: String,
    ) : ApiResponse<GetFee>

    @GET("api/PayWithTransfer/ConfirmStatus")
    suspend fun confirmStatus(
        @Query("institutionCode") institutionCode: String,
        @Query("reference") reference : String,
    ) : ApiResponse<ConfirmStatusResponse>
}