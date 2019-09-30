package com.appzonegroup.app.fasttrack.utility.online;

import com.appzonegroup.app.fasttrack.BuildConfig;
import com.creditclub.core.data.Encryption;

/**
 * Created by fdamilola on 9/5/15.
 * Contact fdamilola@gmail.com or fdamilola@hextremelabs.com or fdamilola@echurch.ng
 */
public class ApiGenerator {
    private static final String CREDIT_CLUB_AGENT_URL_NEW = BuildConfig.API_HOST + "/CreditClubClient/HttpJavaClient/BankOneService.aspx?";
    private static final String CREDIT_CLUB_LOCATION_UPDATE_URL = BuildConfig.API_HOST + "/CreditClubClient/HttpJavaClient/MobileService.aspx";
    private static final String BASE_URL = CREDIT_CLUB_AGENT_URL_NEW;

    private static final String BASE_URL_IMAGE = BuildConfig.HOST + "/creditclub/Httpjavaclient/BankOneImageUploadService.aspx";

    public static final String BASE_URL_LOCATION = CREDIT_CLUB_LOCATION_UPDATE_URL;

    public static String operationInit(String msisdn, String sessionId, String activationCode, String location, boolean page) {
        String finalString = "OPERATION=31e8a9fe53164155&MSISDN="
                + Encryption.encrypt(msisdn) +
                "&SESSION_ID=" + Encryption.encrypt(sessionId) +
                "&ACTIVATION_CODE=" + Encryption.encrypt(activationCode)
                + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location);
        if (page) {
            return BASE_URL + finalString + "&USE_VERIFICATION_CODE=True&VERIFICATION_CODE=" + Encryption.encrypt(activationCode);
        } else {
            return BASE_URL + finalString;
        }
    }

    public static String operationActivation(String msisdn, String sessionId, String activationCode, String location, boolean page) {
        String finalString = "OPERATION=" + Encryption.encrypt("ACTIVATION") + "&MSISDN="
                + Encryption.encrypt(msisdn) +
                "&SESSION_ID=" + Encryption.encrypt(sessionId) +
                "&ACTIVATION_CODE=" + Encryption.encrypt(activationCode)
                + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location);
        if (page) {
            return BASE_URL + finalString + "&USE_VERIFICATION_CODE=True&VERIFICATION_CODE=" + Encryption.encrypt(activationCode);
        } else {
            return BASE_URL + finalString;
        }
    }

    public static String operationNext(String msisdn, String sessionId, String text, String location) {
        String finalString = "OPERATION=baf5ce0ca516124f&MSISDN=" + Encryption.encrypt(msisdn)
                + "&SESSION_ID=" + Encryption.encrypt(sessionId) + "&TEXT="
                + Encryption.encrypt(text) + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location);
        return BASE_URL + finalString;
    }

    public static String operationNextImage(String msisdn, String sessionId, String location, String institutionCode, boolean isFullImage) {
        String finalString = "?OPERATION=NEXT&MSISDN=" + msisdn
                + "&SESSION_ID=" + sessionId + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location)
                + "&INSTITUTION_CODE=" + Encryption.encrypt(institutionCode) + "&FULL_IMAGE=" + String.valueOf(isFullImage);
        return BASE_URL_IMAGE + finalString;
    }

    public static String operationContinue(String msisdn, String sessionId, String text, String location) {
        String finalString = "OPERATION=" + Encryption.encrypt("CONTINUE") + "&MSISDN=" + Encryption.encrypt(msisdn)
                + "&SESSION_ID=" + Encryption.encrypt(sessionId) + "&TEXT="
                + Encryption.encrypt(text) + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location);
        return BASE_URL + finalString;
    }

    public static String operationContinueImage(String msisdn, String sessionId, String location) {
        String finalString = "OPERATION=CONTINUE&MSISDN=" + msisdn
                + "&SESSION_ID=" + sessionId + "&USE_XML=True&GEO_LOCATION=" + Encryption.encrypt(location);
        return BASE_URL_IMAGE + finalString;
    }
}
