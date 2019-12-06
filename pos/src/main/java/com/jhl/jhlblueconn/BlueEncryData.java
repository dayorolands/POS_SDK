package com.jhl.jhlblueconn;

import android.util.Log;

public class BlueEncryData {
    private static final boolean D = true;
    private static final String TAG = "EnDesData";
    public static boolean isReady;

    public static native int EnDesData(byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    static {
        isReady = false;
        try {
            isReady = false;
            System.loadLibrary("bluetransdata");
            isReady = true;
        } catch (Exception e) {
            isReady = false;
            Log.e(TAG, "load bluetransdata Fail");
        }
    }
}
