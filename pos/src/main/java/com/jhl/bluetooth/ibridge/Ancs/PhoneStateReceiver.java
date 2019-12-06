package com.jhl.bluetooth.ibridge.Ancs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PhoneStateReceiver extends BroadcastReceiver {
    private int incomingCallNotificationUID = -1;
    private int previousPhoneState = -1;

    public void onReceive(Context context, Intent intent) {
        Log.i("PhoneStateReceiver", "[Broadcast]" + intent.getAction());
        doReceivePhone(context, intent);
    }

    public void doReceivePhone(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra("incoming_number");
        int state = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
        AppInformation incomingCallAppInformation = GattNotificationManager.sharedInstance().getAppInformation(AncsUtils.APP_PACKAGE_NAME_INCOMING_CALL);
        AppInformation missCallAppInformation = GattNotificationManager.sharedInstance().getAppInformation(AncsUtils.APP_PACKAGE_NAME_MISS_CALL);
        switch (state) {
            case 0:
                Log.i("PhoneStateReceiver", "[Broadcast]电话挂断=" + phoneNumber);
                if (missCallAppInformation != null && this.previousPhoneState == 1) {
                    GattNotificationManager.sharedInstance().removeNotification(this.incomingCallNotificationUID);
                    GattNotification notification = new GattNotification();
                    notification.eventID = 0;
                    notification.eventFlags = 24;
                    notification.categoryID = 2;
                    if (phoneNumber != null) {
                        notification.addAttribute((byte) 1, phoneNumber.getBytes());
                    }
                    notification.addAttribute((byte) 5, new SimpleDateFormat("yyyyMMdd'T'HHmmSS").format(new Date(System.currentTimeMillis())).getBytes());
                    String missCall = missCallAppInformation.displayName;
                    notification.addAttribute((byte) 4, String.format("%d", new Object[]{Integer.valueOf(missCall.length())}).getBytes());
                    notification.addAttribute((byte) 3, missCall.getBytes());
                    notification.addAttribute((byte) 0, AncsUtils.APP_PACKAGE_NAME_MISS_CALL.getBytes());
                    if (missCallAppInformation.negativeString != null) {
                        notification.addAttribute((byte) 7, missCallAppInformation.negativeString.getBytes());
                    }
                    if (missCallAppInformation.positiveString != null) {
                        notification.addAttribute((byte) 6, missCallAppInformation.positiveString.getBytes());
                    }
                    GattNotificationManager.sharedInstance().addNotification(notification);
                }
                this.incomingCallNotificationUID = -1;
                break;
            case 1:
                Log.i("PhoneStateReceiver", "[Broadcast]等待接电话=" + phoneNumber);
                if (!(incomingCallAppInformation == null || this.previousPhoneState == 1)) {
                    GattNotification notification2 = new GattNotification();
                    notification2.eventID = 0;
                    notification2.eventFlags = 25;
                    notification2.categoryID = 1;
                    if (phoneNumber != null) {
                        notification2.addAttribute((byte) 1, phoneNumber.getBytes());
                    }
                    String incomingCall = incomingCallAppInformation.displayName;
                    notification2.addAttribute((byte) 4, String.format("%d", new Object[]{Integer.valueOf(incomingCall.length())}).getBytes());
                    notification2.addAttribute((byte) 3, incomingCall.getBytes());
                    notification2.addAttribute((byte) 0, AncsUtils.APP_PACKAGE_NAME_INCOMING_CALL.getBytes());
                    if (incomingCallAppInformation.negativeString != null) {
                        notification2.addAttribute((byte) 7, incomingCallAppInformation.negativeString.getBytes());
                    }
                    if (incomingCallAppInformation.positiveString != null) {
                        notification2.addAttribute((byte) 6, incomingCallAppInformation.positiveString.getBytes());
                    }
                    GattNotificationManager.sharedInstance().addNotification(notification2);
                    this.incomingCallNotificationUID = notification2.notificationUID;
                    Log.i("PhoneStateReceiver", "incomingCallNotificationUID = " + this.incomingCallNotificationUID);
                    break;
                }
            case 2:
                Log.i("PhoneStateReceiver", "[Broadcast]通话中=" + phoneNumber);
                if (this.previousPhoneState == 1) {
                    GattNotificationManager.sharedInstance().removeNotification(this.incomingCallNotificationUID);
                    this.incomingCallNotificationUID = -1;
                    break;
                }
                break;
        }
        this.previousPhoneState = state;
    }
}
