package com.telpo.tps550.api.util;

public class SystemUtil {
    public static final int PRINTER_HDX = 0;
    public static final int PRINTER_JX2R22 = 5;
    public static final int PRINTER_JX3R02 = 3;
    public static final int PRINTER_JX3R03 = 4;
    public static final int PRINTER_PRT_BAIDU = 2;
    public static final int PRINTER_PRT_COMMON = 1;
    public static final int PRINTER_PT486F08401MB = 6;
    public static final int PRINTER_PT723F08401 = 7;
    public static final int PRINTER_SY581 = 8;
    public static final int READER_AU9540_GBS = 3;
    public static final int READER_AU9540_GCS = 1;
    public static final int READER_AU9560_GBS = 4;
    public static final int READER_AU9560_GCS = 2;
    public static final int READER_MSR = 5;
    public static final int READER_VPOS3583 = 0;

    private static native int get_device_type();

    private static native int get_icc_reader_type();

    private static native int get_printer581_type();

    private static native int get_printer_type();

    static {
        System.loadLibrary("system_util");
    }

    public static int getDeviceType() {
        return get_device_type();
    }

    public static int getICCReaderType() {
        return get_icc_reader_type();
    }

    public static int getPrinterType() {
        return get_printer_type();
    }

    public static int checkPrinter581() {
        return get_printer581_type();
    }
}
