package com.appzonegroup.app.fasttrack.utility;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.appzonegroup.app.fasttrack.model.TransactionCountType;
import com.creditclub.core.util.ContextExtensionsKt;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Joseph on 7/19/2017.
 */
public class Misc {

    public static String getGUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static void populateSpinnerWithString(Activity activity, ArrayList<String> data, Spinner spinner) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, data);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    public static void increaseTransactionMonitorCounter(Context context, TransactionCountType transactionCountType,
                                                         String sessionID) {
        ContextExtensionsKt.increaseTransactionMonitorCounter(
                context,
                com.creditclub.core.type.TransactionCountType.valueOf(transactionCountType.name()),
                sessionID
        );
    }

    public static void resetTransactionMonitorCounter(Context context) {
        ContextExtensionsKt.resetTransactionMonitorCounter(context);
    }
}
