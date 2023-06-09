package com.cluster.core.data.api

import android.util.Log
import com.cluster.core.data.Encryption
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import retrofit2.http.*

interface BankOneService {

    @GET("BankOneService.aspx?OPERATION=")
    suspend fun operationActivation(
        @Query("MSISDN") phoneNumber: String,
        @Query("SESSION_ID") sessionID: String,
        @Query("ACTIVATION_CODE") activationCode: String,
        @Query("GEO_LOCATION") encryptedLocation: String,
        @Query("INSTITUTION_CODE") encryptedInstitutionCode: String?
    ): String?

    @GET("BankOneService.aspx?OPERATION=31e8a9fe53164155")
    suspend fun operationInit(): String?

    @Multipart
    @POST("BankOneImageUploadService.aspx?OPERATION=NEXT&USE_XML=True")
    suspend fun operationNextImage(
        @Query("MSISDN") msisdn: String,
        @Query("SESSION_ID") sessionId: String,
        @Query("GEO_LOCATION") encryptedLocation: String?,
        @Query("INSTITUTION_CODE") encryptedInstitutionCode: String?,
        @Query("FULL_IMAGE") isFullImage: Boolean,
        @Part file: MultipartBody.Part
    ): String?

    @GET
    suspend fun operationGet(@Url url: String): String?

    @POST
    suspend fun operationPost(@Url url: String?, @Body requestBody: RequestBody): String?

    object UrlGenerator : KoinComponent {
        private val apiHost get() = get<AppConfig>().apiHost

        private val BASE_URL =
            "${apiHost}/CreditClubClient/HttpJavaClient/BankOneService.aspx?"

        private val BASE_URL_IMAGE =
            "${apiHost}/CreditClubClient/HttpJavaClient/BankOneImageUploadService.aspx"

        @JvmStatic
        fun operationInit(
            msisdn: String,
            sessionId: String,
            activationCode: String,
            location: String,
            page: Boolean,
            institutionCode: String?
        ): String {
            val finalString =
                ("OPERATION=31e8a9fe53164155&MSISDN=${Encryption.encrypt(msisdn)}&SESSION_ID=${Encryption.encrypt(
                    sessionId
                )}&ACTIVATION_CODE=${Encryption.encrypt(activationCode)}&USE_XML=True&GEO_LOCATION=${Encryption.encrypt(
                    location
                )}&INSTITUTION_CODE=${Encryption.encrypt(
                    institutionCode
                )}")

            return if (page) {
                "$BASE_URL$finalString&USE_VERIFICATION_CODE=True&VERIFICATION_CODE=" + Encryption.encrypt(
                    activationCode
                )
            } else {
                "$BASE_URL$finalString"
            }
        }

        @JvmStatic
        fun operationActivation(
            msisdn: String,
            sessionId: String,
            activationCode: String,
            location: String,
            page: Boolean,
            institutionCode: String?
        ): String {
            val finalString = ("OPERATION=" + Encryption.encrypt("ACTIVATION") + "&MSISDN="
                    + Encryption.encrypt(msisdn) +
                    "&SESSION_ID=" + Encryption.encrypt(sessionId) +
                    "&ACTIVATION_CODE=" + Encryption.encrypt(activationCode)
                    + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location)
                    + "&INSTITUTION_CODE=" + Encryption.encrypt(institutionCode))
            return if (page) {
                "$BASE_URL$finalString&USE_VERIFICATION_CODE=True&VERIFICATION_CODE=" + Encryption.encrypt(
                    activationCode
                )
            } else {
                BASE_URL + finalString
            }
        }

        @JvmStatic
        fun operationNext(
            msisdn: String,
            sessionId: String,
            text: String,
            location: String,
            institutionCode: String?
        ): String {
            val finalString = ("OPERATION=baf5ce0ca516124f&MSISDN=" + Encryption.encrypt(msisdn)
                    + "&SESSION_ID=" + Encryption.encrypt(sessionId) + "&TEXT="
                    + Encryption.encrypt(text) + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(
                location
            )
                    + "&INSTITUTION_CODE=" + Encryption.encrypt(institutionCode))
            return BASE_URL + finalString
        }

        @JvmStatic
        fun operationNextImage(
            msisdn: String,
            sessionId: String,
            location: String,
            institutionCode: String?,
            isFullImage: Boolean
        ): String {
            val finalString = ("?OPERATION=NEXT&MSISDN=" + msisdn
                    + "&SESSION_ID=" + sessionId + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(
                location
            )
                    + "&INSTITUTION_CODE=" + Encryption.encrypt(institutionCode) + "&FULL_IMAGE=" + isFullImage.toString())
            Log.d("Call", finalString)

            return BASE_URL_IMAGE + finalString
        }

        @JvmStatic
        fun operationContinue(
            msisdn: String,
            sessionId: String,
            text: String,
            location: String,
            institutionCode: String?
        ): String {
            val finalString =
                ("OPERATION=" + Encryption.encrypt("CONTINUE") + "&MSISDN=" + Encryption.encrypt(
                    msisdn
                )
                        + "&SESSION_ID=" + Encryption.encrypt(sessionId) + "&TEXT="
                        + Encryption.encrypt(text) + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(
                    location
                )
                        + "&INSTITUTION_CODE=" + Encryption.encrypt(institutionCode))
            return BASE_URL + finalString
        }

        @JvmStatic
        fun operationContinueImage(
            msisdn: String,
            sessionId: String,
            location: String,
            institutionCode: String?
        ): String {
            val finalString = ("OPERATION=CONTINUE&MSISDN=" + msisdn
                    + "&SESSION_ID=" + sessionId + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(
                location
            )
                    + "&INSTITUTION_CODE=" + Encryption.encrypt(institutionCode))
            return BASE_URL_IMAGE + finalString
        }

        @JvmStatic
        fun imageStatusCheck(sessionId: String): String {
            return "$BASE_URL_IMAGE/ImageStatusCheck?sessionID='$sessionId'"
        }
    }
}
