package com.cluster.core.data.api

import com.cluster.core.data.model.AgentLoanRequest
import com.cluster.core.data.response.BackendResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentLoansService {
    @POST("AgentLoans/Process")
    suspend fun process(@Body request: AgentLoanRequest): BackendResponse?
}