package com.jhl.bluetooth.ibridge.Ancs;

import android.util.Log;

import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

class GattNotificationNotify {
    public byte categoryCount = 0;
    public byte categoryID = 0;
    public byte eventFlags = 0;
    public byte eventID = 0;
    public int notificationUID = 0;

    public static GattNotificationNotify parse(byte[] format) {
        Log.i("build", "parse:" + AncsUtils.getPacketString(format));
        if (format == null || format.length != 8) {
            return null;
        }
        GattNotificationNotify notification = new GattNotificationNotify();
        notification.eventID = format[0];
        notification.eventFlags = format[1];
        notification.categoryID = format[2];
        notification.categoryCount = format[3];
        notification.notificationUID = SystemUtils.byteArray2Int(format, 4, 4);
        return notification;
    }

    public GattNotificationNotify() {
    }

    public GattNotificationNotify(byte eventID2, byte eventFlags2, byte categoryID2, byte categoryCount2, int notificationUID2) {
        this.eventID = eventID2;
        this.eventFlags = eventFlags2;
        this.categoryID = categoryID2;
        this.categoryCount = categoryCount2;
        this.notificationUID = notificationUID2;
    }

    public byte[] build() {
        byte[] format = new byte[8];
        format[0] = this.eventID;
        format[1] = this.eventFlags;
        format[2] = this.categoryID;
        format[3] = this.categoryCount;
        SystemUtils.int2ByteArray(this.notificationUID, format, 4, 4);
        Log.i("build", "format:" + AncsUtils.getPacketString(format));
        return format;
    }

    public String toString() {
        return "".concat("notificationUID=" + this.notificationUID + ";").concat("eventID=" + AncsUtils.getEventIDString(this.eventID) + ";").concat("eventFlags=" + AncsUtils.getEventFlags(this.eventFlags) + ";").concat("categoryID=" + AncsUtils.getCategoryIDString(this.categoryID) + ";").concat("categoryCount=" + this.categoryCount + ";");
    }
}
