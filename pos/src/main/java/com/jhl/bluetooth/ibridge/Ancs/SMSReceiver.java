package com.jhl.bluetooth.ibridge.Ancs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] arr$;
        Bundle bundle = intent.getExtras();
        AppInformation smsAppInformation = GattNotificationManager.sharedInstance().getAppInformation(AncsUtils.APP_PACKAGE_NAME_SMS);
        Log.i("SMSReceiver", "[Broadcast]" + intent.getAction());
        if (smsAppInformation == null || bundle == null) {
            Log.i("SMSReceiver", "android.provider.Telephony.SMS_RECEIVED not in write list!");
            return;
        }
        Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage[] mges = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            mges[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
        for (SmsMessage mge : mges) {
            String sender = mge.getDisplayOriginatingAddress();
            String content = mge.getMessageBody();
            String sendTime = new SimpleDateFormat("yyyyMMdd'T'HHmmSS").format(new Date(mge.getTimestampMillis()));
            GattNotification notification = new GattNotification();
            notification.eventID = 0;
            notification.eventFlags = AncsUtils.EVENT_FLAG_NEGATIVE_ACTION;
            notification.categoryID = 4;
            notification.addAttribute((byte) 0, AncsUtils.APP_PACKAGE_NAME_SMS.getBytes());
            if (sender != null) {
                notification.addAttribute((byte) 1, sender.getBytes());
            }
            if (content != null) {
                notification.addAttribute((byte) 4, String.format("%d", new Object[]{Integer.valueOf(content.length())}).getBytes());
                notification.addAttribute((byte) 3, content.getBytes());
            }
            if (sendTime != null) {
                notification.addAttribute((byte) 5, sendTime.getBytes());
            }
            if (smsAppInformation.negativeString != null) {
                notification.addAttribute((byte) 7, smsAppInformation.negativeString.getBytes());
            }
            GattNotificationManager.sharedInstance().addNotification(notification);
        }
    }
}
