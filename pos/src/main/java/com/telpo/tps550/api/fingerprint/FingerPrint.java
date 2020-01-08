package com.telpo.tps550.api.fingerprint;

public class FingerPrint {
    static native int digital_tube(int i);

    static native int fingeric_power(int i);

    static native int fingerprint_power(int i);

    static native int iccard_power(int i);

    static native int idcard_power(int i);

    static native int psam_switch(int i);

    static native int rfid_read();

    static native int usb_port(int i);

    public static boolean fingerPrintPower(int arg) {
        return fingerprint_power(arg) == 0;
    }

    public static boolean digitalTube(int arg) {
        return digital_tube(arg) == 0;
    }

    public static boolean fingericPower(int arg) {
        return fingeric_power(arg) == 0;
    }

    public static boolean usbPort(int arg) {
        return usb_port(arg) == 0;
    }

    public static boolean idcardPower(int arg) {
        return idcard_power(arg) == 0;
    }

    public static boolean iccardPower(int arg) {
        return iccard_power(arg) == 0;
    }

    public static boolean psamSwitch(int arg) {
        return psam_switch(arg) == 0;
    }

    public static int rfidRead() {
        return rfid_read();
    }

    static {
        System.loadLibrary("fingerprint");
    }
}
