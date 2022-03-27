package com.cluster.core.data.api

import com.cluster.core.data.model.*
import com.cluster.core.data.response.AgentActivationResponse
import com.cluster.core.data.response.BackendResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("Auth/Login")
    suspend fun login(@Body loginRequest: LoginRequest): BackendResponse?

    @POST("Auth/Activate")
    suspend fun activate(@Body request: ActivationRequest): AgentActivationResponse?

    @POST("Auth/Verify")
    suspend fun verify(@Body verificationRequest: VerificationRequest): BackendResponse?

    @POST("Auth/ChangePassword")
    suspend fun changePassword(@Body request: PasswordChangeRequest): BackendResponse?

    @POST("Auth/ChangeTransactionPin")
    suspend fun changeTransactionPin(@Body request: TransactionPinChangeRequest): BackendResponse?
}