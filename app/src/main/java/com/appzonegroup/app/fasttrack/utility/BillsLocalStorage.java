package com.appzonegroup.app.fasttrack.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.appzonegroup.app.fasttrack.R;

public class BillsLocalStorage {

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
}