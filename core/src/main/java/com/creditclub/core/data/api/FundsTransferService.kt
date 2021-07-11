package com.creditclub.core.data.api

import com.creditclub.core.data.model.AgentFee
import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.request.DepositRequest
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.data.response.BackendResponse
import com.creditclub.core.data.response.GenericResponse
import com.creditclub.core.data.response.NameEnquiryResponse
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

    @GET("FundsTransfer/GetBanks")
    suspend fun getBanks(@Query("institutionCode") institutionCode: String?): List<Bank>?

    @POST("FundsTransfer/GetTransferFee")
    suspend fun getTransferFee(@Body request: FundsTransferRequest): GenericResponse<AgentFee>?
}