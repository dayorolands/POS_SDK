package com.appzonegroup.creditclub.pos.util;

import com.appzonegroup.creditclub.pos.card.CardMisc;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Joseph on 7/19/2017.
 */
public class Misc extends CardMisc {
    public static String dateToLongString(Date date) {
        Format formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS");
        String part = formatter.format(date);
        part = part + "0000 ";
        return part + new SimpleDateFormat("a").format(date);
    }

    public static Date getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }


    public static String getCurrentDateLongString() {
        return dateToLongString(getCurrentDateTime());
    }

    public static byte[] hexStringToByte(String hex) {
        if(hex == null || hex.length()==0){
            return null;
        }
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toUpperCase().toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    public static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
}
