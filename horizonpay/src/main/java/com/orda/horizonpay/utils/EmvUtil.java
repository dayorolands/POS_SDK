package com.orda.horizonpay.utils;


import com.horizonpay.smartpossdk.aidl.emv.EmvTermConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

/***************************************************************************************************
 *                          Copyright (C),  Shenzhen Horizon Technology Limited                    *
 *                                   http://www.horizonpay.cn                                      *
 ***************************************************************************************************
 * usage           :
 * Version         : 1
 * Author          : Carl
 * Date            : 2019/08/12
 * Modify          : create file
 **************************************************************************************************/
public class EmvUtil {

    private static final String TAG = EmvUtil.class.getName();

    public static final String[] arqcTLVTags = new String[]{
            "9F26",
            "9F27",
            "9F10",
            "9F37",
            "9F36",
            "95",
            "9A",
            "9C",
            "9F02",
            "5F2A",
            "82",
            "9F1A",
            "9F33",
            "9F34",
            "9F35",
            "9F1E",
            "84",
            "9F09",
            "9F63"
    };

    public static final String[] tags = new String[]{
            "5F20",
            "5F30",
            "9F03",
            "9F26",
            "9F27",
            "9F10",
            "9F37",
            "9F36",
            "95",
            "9A",
            "9C",
            "9F02",
            "5F2A",
            "82",
            "9F1A",
            "9F03",
            "9F33",
            "9F34",
            "9F35",
            "9F1E",
            "84",
            "9F09",
            "9F41",
            "9F63",
            "5A",
            "4F",
            "5F24",
            "5F34",
            "5F28",
            "9F12",
            "50",
            "56",
            "57",
            "9F20",
            "9F6B"
    };

    public static byte[] getExampleARPCData() {
        //TODO Data returned by background server ,should be contain 91 tag, if you need to test ARPC
        // such as : 91 0A F9 8D 4B 51 B4 76 34 74 30 30 ,   if need to set 71 and 72  ,Please add this String
        return HexUtil.hexStringToByte("910AF98D4B51B47634743030");
    }
}
