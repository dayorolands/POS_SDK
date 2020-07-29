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
}
