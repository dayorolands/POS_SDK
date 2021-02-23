package com.appzonegroup.app.fasttrack.model;

import com.appzonegroup.app.fasttrack.BuildConfig;

import java.util.Locale;

/**
 * Created by Joseph on 6/3/2016.
 */
public class AppConstants {

    public final static String CATEGORYID = "CATEGORYID";
    public final static String CATEGORYNAME = "CATEGORYNAME";
    public final static String PROPERTYCHANGED = "PROPERTYCHANGED";

    public static String getBaseUrl() {
        return BuildConfig.API_HOST;
    }
}
