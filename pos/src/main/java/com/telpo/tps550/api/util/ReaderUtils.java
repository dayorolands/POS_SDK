package com.telpo.tps550.api.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import com.telpo.tps550.api.collect.Collect;
import java.util.List;
import java.util.regex.Pattern;

public class ReaderUtils {
    static String[] nation_list = {"汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜", "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳", "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "柯尔克孜", "土", "达斡尔", "仫佬", "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒", "乌孜别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔", "独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺", "其他", "外国血统中国籍人士"};

    public static String get_nation(int index) {
        return nation_list[index];
    }

    public static int count_chinese(String str) {
        int count = 0;
        while (Pattern.compile("[\\u4e00-\\u9fa5]").matcher(str).find()) {
            count++;
        }
        return count;
    }

    public static String get_finger_info(Context ctx, byte[] fpData) {
        String fingerInfo;
        if (fpData == null || fpData.length != 1024 || fpData[0] != 67) {
            return "(指纹未读取或不含指纹)";
        }
        String fingerInfo2 = String.valueOf("") + GetFingerName(ctx, fpData[5]);
        if (fpData[4] == 1) {
            fingerInfo = String.valueOf(fingerInfo2) + " 指纹质量= " + String.valueOf(fpData[6]);
        } else {
            fingerInfo = String.valueOf(fingerInfo2) + GetFingerStatus(ctx, fpData[4]);
        }
        String fingerInfo3 = String.valueOf(fingerInfo) + "  ";
        if (fpData[512] != 67) {
            return fingerInfo3;
        }
        String fingerInfo4 = String.valueOf(fingerInfo3) + GetFingerName(ctx, fpData[517]);
        if (fpData[516] == 1) {
            return String.valueOf(fingerInfo4) + " 指纹质量= " + String.valueOf(fpData[518]);
        }
        return String.valueOf(fingerInfo4) + GetFingerStatus(ctx, fpData[516]);
    }

    private static String GetFingerName(Context ctx, int fingerPos) {
        switch (fingerPos) {
            case Collect.TYPE_IR:
                return "右手拇指";
            case Collect.TYPE_FINGERPRINT:
                return "右手食指";
            case 13:
                return "右手中指";
            case 14:
                return "右手环指";
            case 15:
                return "右手小指";
            case 16:
                return "左手拇指";
            case 17:
                return "左手食指";
            case 18:
                return "左手中指";
            case 19:
                return "左手环指";
            case 20:
                return "左手小指";
            case 97:
                return "右手不确定指位";
            case 98:
                return "左手不确定指位";
            case 99:
                return "其他不确定指位";
            default:
                return "指位未知";
        }
    }

    private static String GetFingerStatus(Context ctx, int fingerStatus) {
        switch (fingerStatus) {
            case 1:
                return "注册成功";
            case 2:
                return "注册失败";
            case 3:
                return "未注册";
            case Collect.TYPE_OCR:
                return "注册状态未知";
            default:
                return "注册状态未知";
        }
    }

    public static boolean check_package(Context ctx, String packageName) {
        List<ResolveInfo> infos = ctx.getPackageManager().queryIntentActivities(new Intent().setPackage(packageName), 32);
        if (infos == null || infos.size() < 1) {
            return false;
        }
        return true;
    }

    public static byte get_checksum(byte[] data, int start, int length) {
        byte crc = 0;
        int i = 0;
        while (i < length && start + i < length) {
            crc = (byte) (data[start + i] ^ crc);
            i++;
        }
        return crc;
    }

    public static boolean check_checksum(byte[] data, int start, int length) {
        if (start < 0 || data.length < start + length) {
            return false;
        }
        byte crc = 0;
        int i = 0;
        while (i < length - 1) {
            crc = (byte) (data[start + i] ^ crc);
            i++;
        }
        if (crc == data[start + i]) {
            return true;
        }
        return false;
    }

    public static byte[] merge(byte[] hand, byte[] tail) {
        if (hand == null) {
            return tail;
        }
        byte[] data3 = new byte[(hand.length + tail.length)];
        System.arraycopy(hand, 0, data3, 0, hand.length);
        System.arraycopy(tail, 0, data3, hand.length, tail.length);
        return data3;
    }

    public static String byte2HexString(byte[] data, int start, int len) {
        if (data == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            String hex = Integer.toHexString(data[start + i] & 255).toUpperCase();
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String byte2HexString(byte[] data) {
        if (data == null) {
            return "";
        }
        return byte2HexString(data, 0, data.length);
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        String hexString2 = hexString.toUpperCase();
        if (hexString2.length() % 2 != 0) {
            hexString2 = String.valueOf('0') + hexString2;
        }
        int length = hexString2.length() / 2;
        char[] hexChars = hexString2.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static void idcard_poweron() {
        ShellUtils.execCommand("echo 3 > /sys/class/telpoio/power_status", false);
    }

    public static void idcard_poweroff() {
        ShellUtils.execCommand("echo 4 > /sys/class/telpoio/power_status", false);
    }
}
