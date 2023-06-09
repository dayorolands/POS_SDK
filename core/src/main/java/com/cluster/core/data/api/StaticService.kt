package com.cluster.core.data.api

import com.cluster.core.data.model.*
import com.cluster.core.data.request.*
import com.cluster.core.data.response.ApiResponse
import com.cluster.core.data.response.BackendResponse
import com.cluster.core.data.response.BackendResponseWithPayload
import com.cluster.core.data.response.MiniStatementResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/4/2019.
 * Appzone Ltd
 */
interface StaticService {

    @GET("CreditClubStatic/Status")
    suspend fun status(
        @Query("reference") reference: String,
        @Query("type") type: Int
    ): BackendResponse?

    @GET("CreditClubStatic/GetTransactionStatusByReferenceNumber")
    suspend fun getTransactionStatusByReferenceNumber(
        @Query("deviceNumber") deviceNumber: Int,
        @Query("retrievalReferenceNumber") retrievalReferenceNumber: String,
        @Query("institutionCode") institutionCode: String?,
    ): BackendResponse?

    @GET("api/Institution/GetFeatures")
    suspend fun getInstitutionFeatures(
        @Query("code") institutionCode: String,
        @Query("agentCategory") agentCategory: Int
    ) : GetFeatureResponse?

    @POST("CreditClubStatic/BalanceEnquiry")
    suspend fun balanceEnquiry(@Body request: BalanceEnquiryRequest): Balance?

    @POST("CreditClubStatic/AgentBalanceEnquiry")
    suspend fun agentBalanceEnquiry(@Body request: BalanceEnquiryRequest): Balance?

    @POST("CreditClubStatic/CustomerBalanceEnquiry")
    suspend fun customerBalanceEnquiry(@Body request: BalanceEnquiryRequest): Balance?

    @POST("CreditClubStatic/Deposit")
    suspend fun deposit(@Body request: DepositRequest): BackendResponse?

    @POST("CreditClubStatic/WithDrawal")
    suspend fun withdrawal(@Body request: WithdrawalRequest): BackendResponse?

    @POST("CreditClubStatic/GetWithDrawalFee")
    suspend fun getWithdrawalFee(@Body request: WithdrawalRequest): BackendResponseWithPayload<AgentFee>?

    @POST("CreditClubStatic/GetDepositFee")
    suspend fun getDepositFee(@Body request: DepositRequest): BackendResponseWithPayload<AgentFee>?

    @POST("CreditClubStatic/SendToken")
    suspend fun sendToken(@Body sendTokenRequest: SendTokenRequest): BackendResponse?

    @POST("CreditClubStatic/ConfirmToken")
    suspend fun confirmToken(@Body confirmTokenRequest: ConfirmTokenRequest): BackendResponse?

    @POST("CreditClubStatic/BVNUpdate")
    suspend fun bVNUpdate(@Body request: BVNRequest): BackendResponse?

    @POST("CreditClubStatic/Register")
    suspend fun register(
        @Body request: CustomerRequest,
        @Query("IsRetrial") isRetrial: Boolean = false
    ): BackendResponse?

    @POST("CreditClubStatic/PinChange")
    suspend fun changeCustomerPin(@Body request: CustomerPinChangeRequest): BackendResponse?

    @POST("CreditClubStatic/LoanRequest")
    suspend fun loanRequest(request: LoanRequestCreditClub): BackendResponse?

    @GET("CreditClubStatic/AccountOpeningStatus")
    suspend fun accountOpeningStatus(
        @Query("referenceID") referenceID: String,
        @Query("institutionCode") institutionCode: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String
    ): BackendResponse?

    @GET("CreditClubStatic/GetEligibleLoanProducts")
    suspend fun getEligibleLoanProducts(
        @Query("institutionCode") institutionCode: String,
        @Query("associationID") associationID: String,
        @Query("memberID") memberID: String,
        @Query("customerAccountNumber") customerAccountNumber: String
    ): List<LoanProduct>

    @GET("CreditClubStatic/GetAllProducts")
    suspend fun getAllProducts(
        @Query("institutionCode") institutionCode: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String?
    ): List<Product>?

    @GET("CreditClubStatic/GetAgentDetails")
    suspend fun getAgentDetails(
        @Query("verificationCode") verificationCode: String,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
        @Query("institutionCode") institutionCode: String?
    ): BackendResponse?

    @GET("CreditClubStatic/GetCustomerAccountByPhoneNumber")
    suspend fun getCustomerAccountByPhoneNumber(
        @Query("institutionCode") institutionCode: String?,
        @Query("phoneNumber") phoneNumber: String
    ): CustomerAccount?

    @GET("CreditClubStatic/GetAgentInfoByPhoneNumber")
    suspend fun getAgentInfoByPhoneNumber(
        @Query("institutionCode") institutionCode: String?,
        @Query("phoneNumber") phoneNumber: String?
    ): AgentInfo?

    @GET("CreditClubStatic/GetCustomerAccountByAccountNumber")
    suspend fun getCustomerAccountByAccountNumber(
        @Query("institutionCode") institutionCode: String?,
        @Query("accountNumber") accountNumber: String
    ): AccountInfo?

    @POST("CreditClubStatic/GetCustomerDetailsByBVN")
    suspend fun getCustomerDetailsByBVN(
        @Query("instituionCode") institutionCode: String?,
        @Query("BVN") bvn: String
    ): BVNDetails?

    @POST("CreditClubStatic/MiniStatement")
    suspend fun miniStatement(@Body request: MiniStatementRequest): MiniStatementResponse?

    @POST("CreditClubStatic/SubmitSurvey")
    suspend fun submitSurvey(@Body request: SubmitSurveyRequest)

    @GET("CreditClubStatic/GetBannerImages")
    suspend fun getBannerImages(
        @Query("InstitutionCode") institutionCode: String?,
        @Query("AgentPhoneNumber") agentPhoneNumber: String?,
        @Query("AppVersionName") appVersionName: String?
    ): ApiResponse<List<String>>

    @GET("CreditClubStatic/GetSurveyQuestions")
    suspend fun getSurveyQuestions(
        @Query("InstitutionCode") institutionCode: String?,
        @Query("AgentPhoneNumber") agentPhoneNumber: String?,
        @Query("AppVersionName") appVersionName: String?
    ): ApiResponse<List<SurveyQuestion>>

    @GET("CreditClubStatic/GetStates")
    suspend fun getStates(@Query("institutionCode") institutionCode: String?): ApiResponse<List<State>?>?

    @GET("CreditClubStatic/GetLGAs")
    suspend fun getLgas(
        @Query("institutionCode") institutionCode: String?,
        @Query("stateID") stateId: String?
    ): ApiResponse<List<Lga>?>?
}