package com.telpo.tps550.api.led;

public class LedPowerManager {
    static native int led_power_blue(int i);

    static native int led_power_green(int i);

    static native int led_power_red(int i);

    static native int tps575_fine_contrast(int i);

    static native int tps575_second_lcdclose();

    static native int tps575_second_screen(byte[] bArr);

    static native int tps575_thick_contrast(int i);

    public static boolean power_green(int arg) {
        return led_power_green(arg) == 0;
    }

    public static boolean power_red(int arg) {
        return led_power_red(arg) == 0;
    }

    public static boolean power_blue(int arg) {
        return led_power_blue(arg) == 0;
    }

    public static int tps575_setSecondScreen(byte[] bitmap) {
        return tps575_second_screen(bitmap);
    }

    public static int tps575_setSecondScreenLcdClose() {
        return tps575_second_lcdclose();
    }

    public static int tps575_setThickContrast(int arg) {
        int arg2;
        if (arg == 0) {
            arg2 = 32;
        } else if (arg == 1) {
            arg2 = 33;
        } else if (arg == 2) {
            arg2 = 34;
        } else if (arg == 3) {
            arg2 = 35;
        } else if (arg == 4) {
            arg2 = 36;
        } else if (arg == 5) {
            arg2 = 37;
        } else if (arg == 6) {
            arg2 = 38;
        } else if (arg != 7) {
            return -1;
        } else {
            arg2 = 39;
        }
        return tps575_thick_contrast(arg2);
    }

    public static int tps575_setFineContrast(int arg) {
        if (arg > 63 || arg < 0) {
            return -1;
        }
        return tps575_fine_contrast(arg);
    }

    static {
        System.loadLibrary("ledpower");
    }
}
