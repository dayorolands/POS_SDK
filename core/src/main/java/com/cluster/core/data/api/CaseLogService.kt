package com.cluster.core.data.api

import com.cluster.core.data.model.CaseCategory
import com.cluster.core.data.model.CaseDetail
import com.cluster.core.data.model.CaseLogResult
import com.cluster.core.data.model.Feedback
import com.cluster.core.data.request.CaseDetailsRequest
import com.cluster.core.data.request.CaseMessageThreadRequest
import com.cluster.core.data.request.LogCaseRequest
import com.cluster.core.data.response.CaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface CaseLogService {
    @GET("api/CaseLog/GetProducts")
    suspend fun getProducts(): List<String>?

    @GET("api/CaseLog/GetCaseCategories")
    suspend fun getCaseCategories(@Query("institutionCode") institutionCode: String?): List<CaseCategory>?

    @POST("api/CaseLog/LogCase")
    suspend fun logCase(@Body request: LogCaseRequest): CaseLogResult?

    @POST("api/CaseLog/CaseDetails")
    suspend fun caseDetails(@Body request: CaseDetailsRequest): CaseResponse<List<CaseDetail>>?

    @POST("api/CaseLog/SaveFeedback")
    suspend fun saveFeedback(@Body request: Feedback): CaseResponse<Feedback>?

    @POST("api/CaseLog/GetCaseMessageThread")
    suspend fun getCaseMessageThread(@Body request: CaseMessageThreadRequest): CaseResponse<List<Feedback>>?

    @POST("api/CaseLog/CloseCase")
    suspend fun closeCase(@Query("CaseReference") caseReference: String): CaseResponse<String>?
}