package com.telpo.tps550.api.led;

public class Led {
    public static final int POS_610_GREEN_LED = 0;
    public static final int POS_610_RED_LED = 1;
    public static final int POS_LED_CLOSE = 1;
    public static final int POS_LED_OPEN = 0;
    public static final int POS_W_PRINTER_LED = 3;
    public static final int POS_W_SYSTEM_LED = 2;

    private static native int led_control(int i);

    public static synchronized int ledControl(int ledType, int ledStatus) {
        int i = -1;
        synchronized (Led.class) {
            switch (ledType) {
                case 0:
                    if (ledStatus != 0) {
                        if (ledStatus == 1) {
                            i = led_control(12);
                            break;
                        }
                    } else {
                        i = led_control(11);
                        break;
                    }
                    break;
                case 1:
                    if (ledStatus != 0) {
                        if (ledStatus == 1) {
                            i = led_control(28);
                            break;
                        }
                    } else {
                        i = led_control(27);
                        break;
                    }
                    break;
                case 2:
                    if (ledStatus != 0) {
                        if (ledStatus == 1) {
                            i = led_control(26);
                            break;
                        }
                    } else {
                        i = led_control(25);
                        break;
                    }
                    break;
                case 3:
                    if (ledStatus != 0) {
                        if (ledStatus == 1) {
                            i = led_control(18);
                            break;
                        }
                    } else {
                        i = led_control(17);
                        break;
                    }
                    break;
            }
        }
        return i;
    }

    static {
        System.loadLibrary("led");
    }
}
