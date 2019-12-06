package com.jhl.bluetooth.ibridge.Ancs;

import androidx.core.view.MotionEventCompat;

public class AncsUtils {
    public static final byte ACTION_ID_NEGATICE = 1;
    public static final byte ACTION_ID_POSITIVE = 0;
    public static final byte APP_ATTRIBUTE_ID_DISPLAY_NAME = 0;
    public static final String APP_PACKAGE_NAME_INCOMING_CALL = "android.intent.action.INCOMING_CALL";
    public static final String APP_PACKAGE_NAME_MISS_CALL = "android.intent.action.MISS_CALL";
    public static final String APP_PACKAGE_NAME_SMS = "android.provider.Telephony.SMS_RECEIVED";
    public static final byte ATTRIBUTE_ID_APP_IDENTIFIER = 0;
    public static final byte ATTRIBUTE_ID_DATE = 5;
    public static final byte ATTRIBUTE_ID_MESSAGE = 3;
    public static final byte ATTRIBUTE_ID_MESSAGE_SIZE = 4;
    public static final byte ATTRIBUTE_ID_NEGATIVE_ACTION_LABEL = 7;
    public static final byte ATTRIBUTE_ID_POSITIVE_ACTION_LABEL = 6;
    public static final byte ATTRIBUTE_ID_SUBTITLE = 2;
    public static final byte ATTRIBUTE_ID_TITLE = 1;
    public static final byte CATEGORY_ID_BUSINESS_AND_FINANCE = 9;
    public static final byte CATEGORY_ID_EMAIL = 6;
    public static final byte CATEGORY_ID_ENTERTAINMENT = 11;
    public static final byte CATEGORY_ID_HEALTH_AND_FITNESS = 8;
    public static final byte CATEGORY_ID_INCOMING_CALL = 1;
    public static final byte CATEGORY_ID_LOCATION = 10;
    public static final byte CATEGORY_ID_MISSED_CALL = 2;
    public static final byte CATEGORY_ID_NEWS = 7;
    public static final byte CATEGORY_ID_OTHER = 0;
    public static final byte CATEGORY_ID_SCHEDULE = 5;
    public static final byte CATEGORY_ID_SOCIAL = 4;
    public static final byte CATEGORY_ID_VOICE_MAIL = 3;
    public static final byte COMMAND_ID_GET_APP_ATTRIBUTES = 1;
    public static final byte COMMAND_ID_GET_NOTIFICATION_ATTRIBUTES = 0;
    public static final byte COMMAND_ID_PERFORM_NOTIFICATION_ACTION = 2;
    public static final byte EVENT_FLAG_IMPORTANT = 2;
    public static final byte EVENT_FLAG_NEGATIVE_ACTION = 16;
    public static final byte EVENT_FLAG_POSITIVE_ACTION = 8;
    public static final byte EVENT_FLAG_PREEXISTING = 4;
    public static final byte EVENT_FLAG_SILENT = 1;
    public static final byte EVENT_ID_ADDED = 0;
    public static final byte EVENT_ID_MODIFIED = 1;
    public static final byte EVENT_ID_REMOVED = 2;
    public static final String GATT_ANCS_CONTROL_POINT = "69D1D8F3-45E1-49A8-9821-9BBDFDAAD9D9";
    public static final String GATT_ANCS_DATA_SOURCE = "22EAC6E9-24D6-4BB5-BE44-B36ACE7C7BFB";
    public static final String GATT_ANCS_NOTIFICATION_SOURCE = "9FBF120D-6301-42D9-8C58-25E699A21DBD";
    public static final String GATT_ANCS_SERVICE = "7905F431-B5CE-4E99-A40F-4B1E122D00D0";
    public static final byte SIZE_OF_LENGTH = 2;
    public static final byte SIZE_OF_NOTIFCATIONUID = 4;

    static String getAttrIDString(byte attrID) {
        String str = "";
        switch (attrID) {
            case 0:
                return "app identifier";
            case 1:
                return "title";
            case 2:
                return "subtitle";
            case 3:
                return "message";
            case 4:
                return "message size";
            case 5:
                return "date";
            default:
                return "_unknown_";
        }
    }

    static String getCommandIDString(byte commandID) {
        String str = "";
        switch (commandID) {
            case 0:
                return "get notification attributes";
            case 1:
                return "get application attributes";
            default:
                return "unknown command id : " + Byte.toString(commandID);
        }
    }

    static String getEventIDString(byte eventID) {
        String str = "";
        switch (eventID) {
            case 0:
                return "event added";
            case 1:
                return "event modified";
            case 2:
                return "event removed";
            default:
                return "unknown event id : " + Byte.toString(eventID);
        }
    }

    static String getCategoryIDString(byte categoryID) {
        String str = "";
        switch (categoryID) {
            case 0:
                return "other";
            case 1:
                return "incoming call";
            case 2:
                return "missed call";
            case 3:
                return "voice mail";
            case 4:
                return "social";
            case 5:
                return "schedule";
            case 6:
                return "email";
            case 7:
                return "news";
            case 8:
                return "fitness";
            case MotionEventCompat.ACTION_HOVER_ENTER /*9*/:
                return "finance";
            case 10:
                return "location";
            case 11:
                return "entertainment";
            default:
                return "unknown category id : " + Byte.toString(categoryID);
        }
    }

    static String getEventFlags(byte eventFlags) {
        String temp = "";
        if ((eventFlags & 1) != 0) {
            temp = temp.concat("slient");
        }
        if ((eventFlags & 2) != 0) {
            return temp.concat("important");
        }
        return temp;
    }

    static String getActionIDString(byte actionID) {
        String temp = "";
        switch (actionID) {
            case 0:
                return "Positive";
            case 1:
                return "Negative";
            default:
                return temp;
        }
    }

    static String getPacketString(byte[] format) {
        String string = "";
        for (byte valueOf : format) {
            string = string.concat(String.format("%02x ", new Object[]{Byte.valueOf(valueOf)}));
        }
        return string;
    }
}
