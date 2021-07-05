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
}
