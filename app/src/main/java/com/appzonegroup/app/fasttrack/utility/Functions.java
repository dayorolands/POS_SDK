package com.appzonegroup.app.fasttrack.utility;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Joseph on 6/6/2016.
 */
public class Functions {

    private static Date getCurrentDateWithTime()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),// + 1900,
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.YEAR));

        Date date = calendar.getTime();
        return date;
    }

    public static String doubleToMoneyFormat(double money){
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String moneyString = formatter.format(money);

        return "N" + moneyString.substring(1);
    }

    public static Date FormatStringToDate(String dateString)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date date = formatter.parse(dateString);
            return date;

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String FormatDateToString(Date date)
    {
        SimpleDateFormat d = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        d.toLocalizedPattern();
        return d.format(date);
    }

    public static String getCurrentDateString(){
        return FormatDateToString(getCurrentDateWithTime());
    }

    public static Spinner populateSpinner(Context context, ArrayList<String> data, Spinner spinner){

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, data);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        return spinner;
    }

    public static Spinner populateSpinner(Context context, String[] data, Spinner spinner){

        ArrayList<String> list = new ArrayList( Arrays.asList( data ) );
        return populateSpinner(context, list, spinner);
    }

    public static void setViewBackgroundColor(Context context, View view, int color){
        try {
            view.setBackgroundColor(context.getResources().getColor(color));
        }catch(Exception ex){}
    }
}
