package com.cluster.core.data.api

import com.cluster.core.data.model.*
import com.cluster.core.data.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface CardlessWithdrawalService {

    @GET("api/CrossBankTransaction/GetBanks")
    suspend fun getBanks(
        @Query("institutionCode") institutionCode: String?,
    ) : GetBankResponse

    @POST("api/CrossBankTransaction/ValidateCustomer")
    suspend fun validatingCustomerRequestInfo(
        @Body request: ValidatingCustomerRequest
    ) : CustomerValidationInfoResponse?

    @POST("api/CrossBankTransaction/SendToken")
    suspend fun sendCustomerToken(
        @Body request: SendCustomerTokenRequest
    ) : SendTokenResponse?

    @POST("api/CrossBankTransaction/Transact")
    suspend fun confirmToken(
        @Header("channel") channel: String,
        @Body request: SubmitTokenRequest
    ) : SubmitTokenResponse?

    @GET("api/CrossBankTransaction/GetTransactionDetails")
    suspend fun getTransactionDetails(
        @Header("channel") channel: String,
        @Query("token") agentToken : String?,
        @Query("agentCode") agentCode: String?,
    ) : ApiResponse<GetTransactionDetails>
}