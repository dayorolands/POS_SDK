package com.appzonegroup.app.fasttrack.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.online.AuthResponse;
import com.google.gson.Gson;

/**
 * Created by Joseph on 6/5/2016.
 */
public class LocalStorage {

    private final static String KEY_AUTH = "CACHE_AUTH_KEY";

    public static String GetValueFor(String key, Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.DATA_SOURCE), Context.MODE_PRIVATE);

        return pref.getString(key, null);
    }

    public static void SaveValue(String key, String value, Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(
                context.getString(R.string.DATA_SOURCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Removes the key-value pair from the SharedPreference
     * @param function The key for the value to be removed
     * @param context
     */
    public static void remove(String function, Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.DATA_SOURCE), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(function);
        editor.apply();
    }

    private static String getCacheAuth(Context context) {
        //SharedPreferences sp = this.ctx.getSharedPreferences(CACHE_AUTH, Activity.MODE_PRIVATE);
        return GetValueFor(KEY_AUTH, context);
    }

    public static void saveCacheAuth(String result, Context context) {
        /*SharedPreferences sp = this.ctx.getSharedPreferences(CACHE_AUTH, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_AUTH, result);
        editor.apply();*/
        SaveValue(KEY_AUTH, result, context);
    }

    public static void deleteCacheAuth(Context context){
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.DATA_SOURCE), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    public static boolean isLoggedIn(Context context){
        //boolean isloggedIn = false;

        String cacheAuth = getCacheAuth(context);

        return cacheAuth != null;
        /*if(cacheAuth == null){
            return isloggedIn;
        }else{
            return true;
        }*/
    }

    public static AuthResponse getCachedAuthResponse(Context context){
        String cacheAuth = getCacheAuth(context);
        if(cacheAuth == null){
            return null;
        }

        {
            try {
                return new Gson().fromJson(cacheAuth, AuthResponse.class);//AuthResponse(new JSONObject(cacheAuth));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getPhoneNumber(Context ctx) {
        return GetValueFor(AppConstants.AGENT_PHONE, ctx);
    }

    public static String getInstitutionCode(Context ctx) {
        return GetValueFor(AppConstants.INSTITUTION_CODE, ctx);
    }

    public static String getAgentsPin(Context ctx) {
        return GetValueFor(AppConstants.AGENT_PIN, ctx);
    }

    public static void setAgentsPin(String pin,Context ctx) {
          LocalStorage.SaveValue(AppConstants.AGENT_PIN, pin, ctx);
    }

    public static void setAgentInfo(Context context, String jsonValue)
    {
        SaveValue(AppConstants.AGENT_INFO, jsonValue, context);
    }

    public static String getAgentInfo(Context context)
    {
        return GetValueFor(AppConstants.AGENT_INFO, context);
    }

}
