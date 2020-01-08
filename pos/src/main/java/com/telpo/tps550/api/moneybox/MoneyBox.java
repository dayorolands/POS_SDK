package com.telpo.tps550.api.moneybox;

public class MoneyBox {
    private static native int close_box();

    private static native int open_box();

    public static int open() {
        int ret = open_box();
        if (ret >= 0) {
            return 0;
        }
        if (ret == -1) {
            return -3;
        }
        return -1;
    }

    public static int close() {
        return close_box();
    }

    static {
        System.loadLibrary("moneybox");
    }
}
