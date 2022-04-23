package com.cluster.core.data.api

import com.cluster.core.data.model.AgentFee
import com.cluster.core.data.model.Bank
import com.cluster.core.data.request.FundsTransferRequest
import com.cluster.core.data.response.BackendResponse
import com.cluster.core.data.response.BackendResponseWithPayload
import com.cluster.core.data.response.NameEnquiryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/16/2019.
 * Appzone Ltd
 */
interface FundsTransferService {
    @POST("FundsTransfer/NameEnquiry")
    suspend fun nameEnquiry(@Body request: FundsTransferRequest): NameEnquiryResponse?

    @POST("FundsTransfer/Transfer")
    suspend fun transfer(@Body request: FundsTransferRequest): BackendResponse?

    @POST("FundsTransfer/Requery")
    suspend fun requery(@Body request: FundsTransferRequest): BackendResponse?

    @GET("FundsTransfer/GetBanks")
    suspend fun getBanks(@Query("institutionCode") institutionCode: String?): List<Bank>?

    @POST("FundsTransfer/GetTransferFee")
    suspend fun getTransferFee(@Body request: FundsTransferRequest): BackendResponseWithPayload<AgentFee>?
}