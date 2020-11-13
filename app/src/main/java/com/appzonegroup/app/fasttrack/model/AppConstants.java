package com.appzonegroup.app.fasttrack.model;

import com.appzonegroup.app.fasttrack.BuildConfig;

import java.util.Locale;

/**
 * Created by Joseph on 6/3/2016.
 */
public class AppConstants {

    private static String BASE_URL = BuildConfig.API_HOST;
    private static String API_TOKEN_URL = BASE_URL + "/CreditClubMiddleWareAPI/api/Token/";

    public static String getSterlingHotlistToken() {
        return "11a9532a-a807-468f-a7a8-b2696439a396";
    }

    public static String getCardHotlistUrl(String cardPan, String accountNumber, String hotlistReason, String token) {
        return String.format(Locale.getDefault(),
                "%s/ThirdPartyAPIService/APIService/Cards/Hotlist/%s/%s/%s/%s",
                BASE_URL, cardPan, accountNumber, hotlistReason, token);
    }


    private static String ACCESS_URL = BASE_URL +
            //"62.173.32.45" +
            ":9000/api/CoreBanking/";
    public static String UPDATE_CHECK_DATE = "UPDATE_CHECK_DATE";
    public static String UPDATE_CHECK_TIME_DIFFERENCE = "UPDATE_CHECK_TIME_DIFFERENCE";
    private static String IS_FIRST_LOGIN = "IS_FIRST_LOGIN";
    public static String INSTITUTION_DATA_AVAILABLE = "INSTITUTION_DATA_AVAILABLE";
    public final static String OTHER_DATA = "OTHER_DATA";
    public final static String LOAD_DATA = "LOAD_DATA";
    public final static String ACTIVATED = "ACTIVATED";
    public final static String API_TOKEN = "API_TOKEN";
    public final static String INSTITUTION_CODE = "INSTITUTION_CODE";
    public final static String LOAN_PRODUCTS = "LOAN_PRODUCTS";
    public final static String PRODUCTS = "PRODUCTS";
    public final static String LAST_LOGIN = "LAST_LOGIN";
    public final static String PRESENT_LOGIN = "PRESENT_LOGIN";
    public final static String CUSTOMERS = "CUSTOMERS";
    public final static String ASSOCIATIONS = "ASSOCIATIONS";
    public final static String MOBILE_ACCOUNT_JSON = "MOBILE_ACCOUNT_JSON";
    public final static String AGENT_PIN = "AGENT_PIN";
    public final static String AGENT_PHONE = "AGENT_PHONE";
    public final static String AGENT_CODE = "AGENT_CODE";
    public final static String CATEGORYID = "CATEGORYID";
    public final static String CATEGORYNAME = "CATEGORYNAME";
    public final static String PROPERTYCHANGED = "PROPERTYCHANGED";
    public final static String AGENT_INFO = "AGENT_INFO";
    public final static String AGENT_NAME = "AGENT_NAME";

    public final static String getSuccessCount() {
        return "SUCCESS_COUNT";
    }

    public final static String getNoInternetCount() {
        return "NO_INTERNET_COUNT";
    }

    public final static String getNoResponseCount() {
        return "NO_RESPONSE_COUNT";
    }

    public final static String getErrorResponseCount() {
        return "ERROR_RESPONSE_COUNT";
    }

    public final static String getRequestCount() {
        return "REQUEST_COUNT";
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getAccessUrl() {
        return ACCESS_URL;
    }
    /*public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
    }*/

    public static String getIsFirstLogin() {
        return IS_FIRST_LOGIN;
    }

    /*public static void setIsFirstLogin(String isFirstLogin) {
        IS_FIRST_LOGIN = isFirstLogin;
    }*/

    public static String getLoadData() {
        return LOAD_DATA;
    }

    public static String getInstitutionDataAvailable() {
        return INSTITUTION_DATA_AVAILABLE;
    }

    /*public static void setLoadData(String loadData) {
        LOAD_DATA = loadData;
    }*/

    public static String getApiTokenUrl() {
        return API_TOKEN_URL;
    }

    public static String getEncryptionKey() {
        return "2Wwg6MEUZiRaQmTpoiGFsQ==";
    }

    public static String getSessionID() {
        return "SESSION_ID";
    }

    public static String generateReportsUrl(String agentPhoneNumber, String institutionCode, String transactionType, String dateStart, String dateEnd, String startIndex, String maxSize) {
        String url = BASE_URL + "/CreditClubMiddleWareAPI/api/Report/GetTransactions?";

        return url + String.format("agentPhoneNumber=%s&institutionCode=%s&transactionType=%s&from=%s&to=%s&status=Successful&startIndex=%s&maxSize=%s", agentPhoneNumber, institutionCode, transactionType, dateStart, dateEnd, startIndex, maxSize);
    }
}
