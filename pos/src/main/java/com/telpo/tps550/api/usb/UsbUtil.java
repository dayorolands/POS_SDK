package com.telpo.tps550.api.usb;

public class UsbUtil {
    public static final int ID_IC_CARD = 0;
    public static final int ID_PSAM_CARD1 = 1;
    public static final int ID_PSAM_CARD2 = 2;

    public static native String getUsbDevice(int i);

    public static native String getUsbDevicehub(int i, int i2);

    static {
        System.loadLibrary("usb_util");
    }
}
