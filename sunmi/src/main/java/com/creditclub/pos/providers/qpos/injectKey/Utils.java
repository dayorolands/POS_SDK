package com.appzonegroup.creditclub.pos.provider.qpos.injectKey;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


public class Utils {
    private static String digits = "0123456789abcdef";

    public Utils() {
    }

    public static String toHex(byte[] data, int length) {
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i != length; ++i) {
            int v = data[i] & 255;
            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 15));
        }

        return buf.toString();
    }

    public static String toHex(byte[] data) {
        return toHex(data, data.length);
    }

    public static byte[] int2Byte(int intValue) {
        byte[] b = new byte[4];
        byte[] r = new byte[4];

        for(int i = 0; i < 4; ++i) {
            b[i] = (byte)(intValue >> 8 * (3 - i) & 255);
        }

        r[3] = b[0];
        r[2] = b[1];
        r[1] = b[2];
        r[0] = b[3];
        return r;
    }

    public static int byte2Int(byte[] b) {
        int intValue = 0;

        for(int i = 0; i < b.length; ++i) {
            intValue += (b[i] & 255) << 8 * (3 - i);
        }

        return intValue;
    }
}
