package com.appzonegroup.creditclub.pos.provider.qpos.utils;

import android.content.Context;

/**
 * Created by dsppc11 on 2018/7/24.
 */





public class ConfigUtil {
    public static void saveUpdateEmvStatis(Context context,boolean value){
        PreferenceUtil.putBoolean(context,Constatns.EMV_UPDATE_STATUS,value);
    }

    public static boolean getUpdateEmvStatis(Context context){
        return PreferenceUtil.getBoolean(context,Constatns.EMV_UPDATE_STATUS,false);
    }




    public static boolean hasReadXml(Context context) {
        return PreferenceUtil.getBoolean(context,Constatns.HAS_EMV_READ_STATUS,false);
    }

    public static void putReadXmlStatus(Context context, boolean b) {
        PreferenceUtil.putBoolean(context,Constatns.HAS_EMV_READ_STATUS,b);
    }
}
