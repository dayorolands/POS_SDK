package com.creditclub.core.data.api

import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.data.model.Biller
import com.creditclub.core.data.request.PayBillRequest
import com.creditclub.core.data.response.PayBillResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 8/5/2019.
 * Appzone Ltd
 */
interface BillsPaymentService {

    @GET("api/PayBills/GetBillers")
    suspend fun getBillers(@Query("institutionCode") institutionCode: String?, @Query("billerCategoryID") billerCategoryID: String): List<Biller>?

    @GET("api/PayBills/GetBillerCategories")
    suspend fun getBillerCategories(@Query("institutionCode") institutionCode: String?): List<BillCategory>?

    @GET("api/PayBills/GetPaymentItems")
    suspend fun getPaymentItems(@Query("institutionCode") institutionCode: String?): Any

    @POST("api/PayBills/RunTransaction")
    suspend fun runTransaction(@Body body: PayBillRequest): PayBillResponse?
}