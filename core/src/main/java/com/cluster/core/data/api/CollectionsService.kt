package com.cluster.core.data.api

import com.cluster.core.data.model.*
import com.cluster.core.data.request.CollectionCustomerValidationRequest
import com.cluster.core.data.request.CollectionPaymentRequest
import com.cluster.core.data.request.CollectionReferenceGenerationRequest
import com.cluster.core.data.response.CollectionGetFeeResponse
import com.cluster.core.data.response.CollectionPaymentResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CollectionsService {
    @POST("api/Collections/CollectionPayment")
    suspend fun collectionPayment(
        @Body request: CollectionPaymentRequest
    ): CollectionPaymentResponse?

    @POST("api/Collections/GenerateCollectionReference")
    suspend fun generateCollectionReference(
        @Body request: CollectionReferenceGenerationRequest
    ): CollectionReference?

    @GET("api/Collections/GetCollectionCategories")
    suspend fun getCollectionCategories(
        @Query("institutionCode") institutionCode: String?,
        @Query("collectionType") collectionType: String?,
        @Query("region") region: String?,
        @Query("collectionService") collectionService: String?
    ): List<CollectionCategory>?

    @GET("api/Collections/GetCollectionCustomer")
    suspend fun getCollectionCustomer(
        @Query("institutionCode") institutionCode: String?,
        @Query("customerID") customerId: String?,
        @Query("region") region: String?,
        @Query("collectionService") collectionService: String?
    ): CollectionCustomer?

    @GET("api/Collections/GetCollectionPaymentItems")
    suspend fun getCollectionPaymentItems(
        @Query("institutionCode") institutionCode: String?,
        @Query("collectionCategoryCode") collectionCategoryCode: String?,
        @Query("region") region: String?,
        @Query("collectionService") collectionService: String?
    ): List<CollectionPaymentItem>?

    @GET("api/Collections/GetCollectionReferenceByReference")
    suspend fun getCollectionReferenceByReference(
        @Query("institutionCode") institutionCode: String?,
        @Query("reference") reference: String?,
        @Query("region") region: String?,
        @Query("collectionService") collectionService: String?,
        @Query("collectiontype") collectionType: String?
    ): CollectionReference?

    @GET("api/Collections/GetCollectionRegions")
    suspend fun getCollectionRegions(
        @Query("institutionCode") institutionCode: String?,
        @Query("collectionService") collectionService: String?
    ): List<String>?

    @GET("api/Collections/GetCollectionTypes")
    suspend fun getCollectionTypes(
        @Query("institutionCode") institutionCode: String?,
        @Query("region") region: String?,
        @Query("collectionService") collectionService: String?
    ): List<String>?

    @GET("api/Collections/VerifyCollectionPayment")
    suspend fun verifyCollectionPayment(
        @Query("institutionCode") institutionCode: String?,
        @Query("paymentReference") paymentReference: String?,
        @Query("region") region: String?,
        @Query("collectionService") collectionService: String?
    ): CollectionPaymentResponse?

    @GET("api/Collections/GetBillers")
    suspend fun getCollectionBillers(
        @Query("institutionCode") institutionCode: String?,
        @Query("collectionService") collectionService: String?
    ): List<CollectionCategory>

    @GET("api/Collections/GetBillerItems")
    suspend fun getCollectionPaymentItems(
        @Query("institutionCode") institutionCode: String?,
        @Query("billerId") billerId: String?
    ): List<CollectionPaymentItem>

    @POST("api/Collections/ValidateCustomer")
    suspend fun validateCustomer(
        @Body request: CollectionCustomerValidationRequest
    ) : CustomerValidationResponse?

    @POST("api/Collections/Payment")
    suspend fun completePayment(
        @Body request: CollectionPaymentRequest
    ): CollectionPaymentResponse?

    @GET("api/Collections/Fee")
    suspend fun getTransactionFee(
        @Query("institutionCode") institutionCode: String?,
        @Query("agentPhoneNumber") agentPhoneNumber: String,
        @Query("amount") amount: Double
    ): CollectionGetFeeResponse
}