package com.dspread.qpos.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dsppc11 on 2018/7/24.
 */

public class PreferenceUtil {

    private static SharedPreferences getSharePre(Context context) {
        return context.getSharedPreferences("QPOS",Context.MODE_PRIVATE);
    }


   public static void putLong(Context context,String name, long value){
       SharedPreferences preferences = getSharePre(context);
       SharedPreferences.Editor edit = preferences.edit();
       edit.putLong(name,value).commit();
   }

    public static long getLong(Context context,String name, long deValue){
        SharedPreferences preferences = getSharePre(context);
        long result = preferences.getLong(name, deValue);
        return result;
    }



    public static void putBoolean(Context context,String name, boolean value){
        SharedPreferences preferences = getSharePre(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(name,value).commit();
    }

    public static boolean getBoolean(Context context,String name, boolean deValue){
        SharedPreferences preferences = getSharePre(context);
        boolean result = preferences.getBoolean(name, deValue);
        return result;
    }

}
