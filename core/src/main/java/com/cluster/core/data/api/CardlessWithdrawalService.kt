package com.cluster.core.data.api

import com.cluster.core.data.model.GetBankResponse
import com.cluster.core.data.model.SendCustomerTokenRequest
import com.cluster.core.data.model.SendTokenResponse
import com.cluster.core.data.model.ValidatingCustomerRequest
import retrofit2.http.Body
import retrofit2.http.GET
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
}