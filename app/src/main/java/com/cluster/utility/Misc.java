package com.cluster.utility;

import android.content.Context;

import com.cluster.model.TransactionCountType;
import com.creditclub.core.util.ContextExtensionsKt;

import java.util.UUID;

/**
 * Created by Joseph on 7/19/2017.
 */
public class Misc {

    public static String getGUID() {
        return UUID.randomUUID().toString().substring(0, 8);
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
