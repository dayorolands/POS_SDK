package com.jhl.bluetooth.ibridge.Tools;

public class SystemUtils {
    public static final String MTK_PLATFORM_KEY = "ro.mediatek.platform";

    public static boolean int2ByteArray(int intValue, byte[] b, int pos, int length) {
        if (((double) intValue) >= Math.pow(2.0d, (double) (length * 8))) {
            return false;
        }
        for (int i = length - 1; i >= 0; i--) {
            b[pos + i] = (byte) ((intValue >> (i * 8)) & 255);
        }
        return true;
    }

    public static int byteArray2Int(byte[] b, int pos, int length) {
        int intValue = 0;
        for (int i = length - 1; i >= 0; i--) {
            intValue += (b[pos + i] & 255) << (i * 8);
        }
        return intValue;
    }

    public static String get(String key) throws IllegalArgumentException {
        String str = "";
        try {
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            return (String) SystemProperties.getMethod("get", new Class[]{String.class}).invoke(SystemProperties, new Object[]{new String(key)});
        } catch (IllegalArgumentException ie) {
            throw ie;
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isMediatekPlatform() {
        String platform = get(MTK_PLATFORM_KEY);
        return platform != null && (platform.startsWith("MT") || platform.startsWith("mt"));
    }
}
