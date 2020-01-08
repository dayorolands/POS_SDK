package com.telpo.tps550.api.util;

import android.util.Log;
import com.telpo.tps550.api.fingerprint.FingerPrint;

public class CommonUtil {
    public static boolean fingerPrintPower(int arg) {
        return FingerPrint.fingerPrintPower(arg);
    }

    public static boolean digitalTube(int arg) {
        return FingerPrint.digitalTube(arg);
    }

    public static boolean usbPort(int arg) {
        return FingerPrint.usbPort(arg);
    }

    public static boolean idcardPower(int arg) {
        return FingerPrint.idcardPower(arg);
    }

    public static boolean iccardPower(int arg) {
        return FingerPrint.iccardPower(arg);
    }

    public static boolean psamSwitch(int arg) {
        return FingerPrint.psamSwitch(arg);
    }

    public static String rfidRead() {
        int result = FingerPrint.rfidRead();
        Log.d("idcard demo", "FingerPrint.rfidRead():" + result);
        String binary = Integer.toBinaryString(result);
        Log.d("idcard demo", "binary:" + binary);
        int binarySize = binary.length();
        if (binarySize < 18) {
            return "读卡失败";
        }
        return Integer.valueOf(binary.substring(1, binarySize - 1), 2).toString();
    }

    private static boolean checkBinary(String checkSum1, String before17, String checkSum2, String after17) {
        int beforeCount = 0;
        int afterCount = 0;
        boolean checkSum1Ok = false;
        boolean checkSum2Ok = false;
        for (int i = 0; i < before17.length(); i++) {
            if (before17.charAt(i) == '1') {
                beforeCount++;
            }
        }
        for (int i2 = 0; i2 < after17.length(); i2++) {
            if (after17.charAt(i2) == '1') {
                afterCount++;
            }
        }
        if (Integer.toString(beforeCount % 2).equals(checkSum1)) {
            checkSum1Ok = true;
        }
        if (!Integer.toString(afterCount % 2).equals(checkSum2)) {
            checkSum2Ok = true;
        }
        if (!checkSum1Ok || !checkSum2Ok) {
            return false;
        }
        return true;
    }
}
