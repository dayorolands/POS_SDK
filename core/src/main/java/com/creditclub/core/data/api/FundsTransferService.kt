package com.creditclub.core.data.api

import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.request.FundsTransferCustomerRequest
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.data.response.BackendResponse
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

    @POST("FundsTransfer/Requery")
    suspend fun requery(@Body request: FundsTransferRequest): BackendResponse?

    @GET("FundsTransfer/GetBanks")
    suspend fun getBanks(@Query("institutionCode") institutionCode: String?): List<Bank>?

    @POST("FundsTransfer/Initiate")
    suspend fun initiate(@Body request: FundsTransferCustomerRequest): BackendResponse?

    @POST("FundsTransfer/ValidateCustomerFundsTransferRequest")
    suspend fun validateCustomerFundsTransferRequest(@Body request: FundsTransferCustomerRequest): BackendResponse?
}