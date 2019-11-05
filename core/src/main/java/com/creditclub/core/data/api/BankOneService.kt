package com.creditclub.core.data.api

import android.util.Log
import com.creditclub.core.BuildConfig
import com.creditclub.core.data.Encryption
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Query

interface BankOneService {

    @GET("BankOneService.aspx?OPERATION=")
    suspend fun operationActivation(
        @Query("MSISDN") phoneNumber: String,
        @Query("SESSION_ID") sessionID: String,
        @Query("ACTIVATION_CODE") activationCode: String,
        @Query("GEO_LOCATION") location: String,
        @Query("INSTITUTION_CODE") institutionCode: String?
    ): ResponseBody

    @GET("BankOneService.aspx?OPERATION=31e8a9fe53164155")
    suspend fun operationInit(): ResponseBody

    object UrlGenerator {
        private const val PRODUCTION_URL = BuildConfig.HOST + "/"
        private const val CREDIT_CLUB_AGENT_URL_NEW =
            PRODUCTION_URL + "CreditClubClient/HttpJavaClient/BankOneService.aspx?"
        private const val CREDIT_CLUB_LOCATION_UPDATE_URL =
            PRODUCTION_URL + "CreditClub/HttpJavaClient/MobileService.aspx"

        private const val BASE_URL = CREDIT_CLUB_AGENT_URL_NEW
        private const val BASE_URL_IMAGE =
            BuildConfig.API_HOST + "/CreditClubClient/HttpJavaClient/BankOneImageUploadService.aspx"

        @JvmStatic
        val BASE_URL_LOCATION = CREDIT_CLUB_LOCATION_UPDATE_URL

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
                    + Encryption.encrypt(text) + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location)
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
                    + "&SESSION_ID=" + sessionId + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location)
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
            val finalString = ("OPERATION=" + Encryption.encrypt("CONTINUE") + "&MSISDN=" + Encryption.encrypt(msisdn)
                    + "&SESSION_ID=" + Encryption.encrypt(sessionId) + "&TEXT="
                    + Encryption.encrypt(text) + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location)
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
                    + "&SESSION_ID=" + sessionId + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location)
                    + "&INSTITUTION_CODE=" + Encryption.encrypt(institutionCode))
            return BASE_URL_IMAGE + finalString
        }

        @JvmStatic
        fun imageStatusCheck(sessionId: String): String {
            return "$BASE_URL_IMAGE/ImageStatusCheck?sessionID='$sessionId'"
        }
    }
}
