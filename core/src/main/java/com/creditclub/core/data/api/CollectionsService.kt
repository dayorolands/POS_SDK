package com.creditclub.core.data.api

import com.creditclub.core.data.model.CollectionCategory
import com.creditclub.core.data.model.CollectionCustomer
import com.creditclub.core.data.model.CollectionPaymentItem
import com.creditclub.core.data.model.CollectionReference
import com.creditclub.core.data.request.CollectionPaymentRequest
import com.creditclub.core.data.request.CollectionReferenceGenerationRequest
import com.creditclub.core.data.response.CollectionPaymentResponse
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
        @Query("collectionService") collectionService: String?
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
}