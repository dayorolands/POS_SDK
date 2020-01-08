package com.zkteco.android.IDReader;

public class WLTService {
    public static int imgHeight = 126;
    public static int imgLength = 38556;
    public static int imgWidth = 102;

    public static native int wlt2Bmp(byte[] bArr, byte[] bArr2);

    static {
        System.loadLibrary("wlt2bmp");
        System.loadLibrary("zkwltdecode");
    }
}
