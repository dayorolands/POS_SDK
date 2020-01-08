package com.telpo.tps550.api.util;

public class StringUtil {

    public enum DeviceModelEnum {
        FFP2,
        TPM301,
        TPS200,
        TPS350,
        TPS350_5800,
        TPS350_4G,
        TPS350L,
        TPS358,
        TPS360,
        TPS360A,
        TPS360C,
        TPS360IC,
        TPS365,
        TPS390,
        TPS390A,
        TPS390F,
        TPS390U,
        TPS400,
        TPS400A,
        TPS400B,
        TPS400C,
        TPS450,
        TPS450C,
        TPS462,
        TPS464,
        TPS465,
        TPS468,
        TPS470,
        TPS480,
        TPS506,
        TPS510,
        TPS510A,
        TPS510C,
        TPS510A_NHW,
        TPS510D,
        TPS513,
        TPS515,
        TPS515A,
        TPS515B,
        TPS515C,
        TPS515D,
        TPS520,
        TPS520A,
        TPS550,
        TPS550A,
        TPS550MTK,
        TPS550P,
        TPS550S,
        TPS570,
        TPS570A,
        TPS573,
        TPS574,
        TPS575,
        TPS580,
        TPS580A,
        TPS580ACRM,
        TPS586,
        TPS586A,
        TPS590,
        TPS610,
        TPS611,
        TPS612,
        TPS613,
        TPS615,
        TPS616,
        TPS617,
        TPS618,
        TPS650,
        TPS650T,
        TPS680,
        TPS681,
        TPS721,
        TPS900,
        TPS900B,
        TPS900MB
    }

    public static String toHexString(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : data) {
            String string = Integer.toHexString(b & 255);
            if (string.length() == 1) {
                stringBuilder.append("0");
            }
            stringBuilder.append(string.toUpperCase());
        }
        return stringBuilder.toString();
    }

    public static byte[] toBytes(String string) {
        String str;
        int len;
        String s = string.toUpperCase();
        int len2 = s.length();
        if (len2 % 2 == 1) {
            str = String.valueOf(s) + "0";
            len = (len2 + 1) >> 1;
        } else {
            str = s;
            len = len2 >> 1;
        }
        byte[] bytes = new byte[len];
        int i = 0;
        int j = 0;
        while (i < len) {
            bytes[i] = (byte) (((byte) ("0123456789ABCDEF".indexOf(str.charAt(j)) << 4)) | ((byte) "0123456789ABCDEF".indexOf(str.charAt(j + 1))));
            i++;
            j += 2;
        }
        return bytes;
    }
}
