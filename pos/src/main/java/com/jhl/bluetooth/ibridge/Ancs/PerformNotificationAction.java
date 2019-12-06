package com.jhl.bluetooth.ibridge.Ancs;

import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

class PerformNotificationAction {
    public static byte CommandID = 2;
    public byte actionID;
    public int notificationUID;

    PerformNotificationAction() {
    }

    public static PerformNotificationAction parse(byte[] format) {
        if (format == null || format.length <= 0 || format[0] != CommandID) {
            return null;
        }
        PerformNotificationAction performNotificationAction = new PerformNotificationAction();
        int position = 0 + 1;
        performNotificationAction.notificationUID = SystemUtils.byteArray2Int(format, position, 4);
        int position2 = position + 4;
        performNotificationAction.actionID = format[position2];
        int position3 = position2 + 1;
        return performNotificationAction;
    }

    public void PerformNotificationAction() {
    }

    public byte[] build() {
        byte[] format = new byte[6];
        format[0] = CommandID;
        int position = 0 + 1;
        SystemUtils.int2ByteArray(this.notificationUID, format, position, 4);
        int position2 = position + 4;
        format[position2] = this.actionID;
        int position3 = position2 + 1;
        return format;
    }

    public String toString() {
        return "".concat("notificationUID=" + this.notificationUID + ";").concat("actionID=" + AncsUtils.getActionIDString(this.actionID));
    }
}
