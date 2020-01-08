package com.telpo.tps550.api.idcard;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.util.StringUtil;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class UsbIdCard {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final byte[] SAM_HEADER = {-86, -86, -86, -106, 105};
    private static int contentLength;
    private static int fplength;
    private static UsbDevice idcard_reader = null;
    private static int imageDatalength;
    private static UsbManager mUsbManager = null;
    static String[] nation_list = {"汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜", "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳", "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "柯尔克孜", "土", "达斡尔", "仫佬", "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒", "乌孜别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔", "独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺", "其他", "外国血统中国籍人士"};
    private static UsbDevice updater = null;
    private final int CHECK = 1;
    private final int DOWNLOAD_1 = 2;
    private final int DOWNLOAD_FINISH = 3;
    private final int NEW_PID = IdCard.READER_PID_WINDOWS;
    private final int NEW_VID = IdCard.READER_VID_WINDOWS;
    private final int REQUEST = 0;
    private int checkCount = 0;
    private long[] crc_16tab;
    private boolean finalRet = false;
    private Context mContext;
    private PendingIntent mPermissionIntent = null;
    private byte[] resultCommand = null;

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v4, resolved type: long[]} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public UsbIdCard(android.content.Context r14) throws com.telpo.tps550.api.TelpoException {
        /*
            r13 = this;
            r12 = 650(0x28a, float:9.11E-43)
            r11 = 3
            r9 = 2
            r8 = 1
            r10 = 0
            r13.<init>()
            r5 = 0
            r13.mPermissionIntent = r5
            r5 = 0
            r13.resultCommand = r5
            r13.REQUEST = r10
            r13.CHECK = r8
            r13.DOWNLOAD_1 = r9
            r13.DOWNLOAD_FINISH = r11
            r13.NEW_PID = r12
            r5 = 10473(0x28e9, float:1.4676E-41)
            r13.NEW_VID = r5
            r13.checkCount = r10
            r13.finalRet = r10
            r5 = 256(0x100, float:3.59E-43)
            long[] r5 = new long[r5]
            r6 = 4129(0x1021, double:2.04E-320)
            r5[r8] = r6
            r6 = 8258(0x2042, double:4.08E-320)
            r5[r9] = r6
            r6 = 12387(0x3063, double:6.12E-320)
            r5[r11] = r6
            r6 = 4
            r8 = 16516(0x4084, double:8.16E-320)
            r5[r6] = r8
            r6 = 5
            r8 = 20645(0x50a5, double:1.02E-319)
            r5[r6] = r8
            r6 = 6
            r8 = 24774(0x60c6, double:1.224E-319)
            r5[r6] = r8
            r6 = 7
            r8 = 28903(0x70e7, double:1.428E-319)
            r5[r6] = r8
            r6 = 8
            r8 = 33032(0x8108, double:1.632E-319)
            r5[r6] = r8
            r6 = 9
            r8 = 37161(0x9129, double:1.836E-319)
            r5[r6] = r8
            r6 = 10
            r8 = 41290(0xa14a, double:2.04E-319)
            r5[r6] = r8
            r6 = 11
            r8 = 45419(0xb16b, double:2.244E-319)
            r5[r6] = r8
            r6 = 12
            r8 = 49548(0xc18c, double:2.448E-319)
            r5[r6] = r8
            r6 = 13
            r8 = 53677(0xd1ad, double:2.652E-319)
            r5[r6] = r8
            r6 = 14
            r8 = 57806(0xe1ce, double:2.856E-319)
            r5[r6] = r8
            r6 = 15
            r8 = 61935(0xf1ef, double:3.06E-319)
            r5[r6] = r8
            r6 = 16
            r8 = 4657(0x1231, double:2.301E-320)
            r5[r6] = r8
            r6 = 17
            r8 = 528(0x210, double:2.61E-321)
            r5[r6] = r8
            r6 = 18
            r8 = 12915(0x3273, double:6.381E-320)
            r5[r6] = r8
            r6 = 19
            r8 = 8786(0x2252, double:4.341E-320)
            r5[r6] = r8
            r6 = 20
            r8 = 21173(0x52b5, double:1.0461E-319)
            r5[r6] = r8
            r6 = 21
            r8 = 17044(0x4294, double:8.421E-320)
            r5[r6] = r8
            r6 = 22
            r8 = 29431(0x72f7, double:1.4541E-319)
            r5[r6] = r8
            r6 = 23
            r8 = 25302(0x62d6, double:1.2501E-319)
            r5[r6] = r8
            r6 = 24
            r8 = 37689(0x9339, double:1.8621E-319)
            r5[r6] = r8
            r6 = 25
            r8 = 33560(0x8318, double:1.6581E-319)
            r5[r6] = r8
            r6 = 26
            r8 = 45947(0xb37b, double:2.2701E-319)
            r5[r6] = r8
            r6 = 27
            r8 = 41818(0xa35a, double:2.0661E-319)
            r5[r6] = r8
            r6 = 28
            r8 = 54205(0xd3bd, double:2.6781E-319)
            r5[r6] = r8
            r6 = 29
            r8 = 50076(0xc39c, double:2.4741E-319)
            r5[r6] = r8
            r6 = 30
            r8 = 62463(0xf3ff, double:3.0861E-319)
            r5[r6] = r8
            r6 = 31
            r8 = 58334(0xe3de, double:2.8821E-319)
            r5[r6] = r8
            r6 = 32
            r8 = 9314(0x2462, double:4.6017E-320)
            r5[r6] = r8
            r6 = 33
            r8 = 13379(0x3443, double:6.61E-320)
            r5[r6] = r8
            r6 = 34
            r8 = 1056(0x420, double:5.217E-321)
            r5[r6] = r8
            r6 = 35
            r8 = 5121(0x1401, double:2.53E-320)
            r5[r6] = r8
            r6 = 36
            r8 = 25830(0x64e6, double:1.27617E-319)
            r5[r6] = r8
            r6 = 37
            r8 = 29895(0x74c7, double:1.477E-319)
            r5[r6] = r8
            r6 = 38
            r8 = 17572(0x44a4, double:8.6817E-320)
            r5[r6] = r8
            r6 = 39
            r8 = 21637(0x5485, double:1.069E-319)
            r5[r6] = r8
            r6 = 40
            r8 = 42346(0xa56a, double:2.09217E-319)
            r5[r6] = r8
            r6 = 41
            r8 = 46411(0xb54b, double:2.293E-319)
            r5[r6] = r8
            r6 = 42
            r8 = 34088(0x8528, double:1.68417E-319)
            r5[r6] = r8
            r6 = 43
            r8 = 38153(0x9509, double:1.885E-319)
            r5[r6] = r8
            r6 = 44
            r8 = 58862(0xe5ee, double:2.90817E-319)
            r5[r6] = r8
            r6 = 45
            r8 = 62927(0xf5cf, double:3.109E-319)
            r5[r6] = r8
            r6 = 46
            r8 = 50604(0xc5ac, double:2.50017E-319)
            r5[r6] = r8
            r6 = 47
            r8 = 54669(0xd58d, double:2.701E-319)
            r5[r6] = r8
            r6 = 48
            r8 = 13907(0x3653, double:6.871E-320)
            r5[r6] = r8
            r6 = 49
            r8 = 9842(0x2672, double:4.8626E-320)
            r5[r6] = r8
            r6 = 50
            r8 = 5649(0x1611, double:2.791E-320)
            r5[r6] = r8
            r6 = 51
            r8 = 1584(0x630, double:7.826E-321)
            r5[r6] = r8
            r6 = 52
            r8 = 30423(0x76d7, double:1.5031E-319)
            r5[r6] = r8
            r6 = 53
            r8 = 26358(0x66f6, double:1.30226E-319)
            r5[r6] = r8
            r6 = 54
            r8 = 22165(0x5695, double:1.0951E-319)
            r5[r6] = r8
            r6 = 55
            r8 = 18100(0x46b4, double:8.9426E-320)
            r5[r6] = r8
            r6 = 56
            r8 = 46939(0xb75b, double:2.3191E-319)
            r5[r6] = r8
            r6 = 57
            r8 = 42874(0xa77a, double:2.11826E-319)
            r5[r6] = r8
            r6 = 58
            r8 = 38681(0x9719, double:1.9111E-319)
            r5[r6] = r8
            r6 = 59
            r8 = 34616(0x8738, double:1.71026E-319)
            r5[r6] = r8
            r6 = 60
            r8 = 63455(0xf7df, double:3.1351E-319)
            r5[r6] = r8
            r6 = 61
            r8 = 59390(0xe7fe, double:2.93426E-319)
            r5[r6] = r8
            r6 = 62
            r8 = 55197(0xd79d, double:2.7271E-319)
            r5[r6] = r8
            r6 = 63
            r8 = 51132(0xc7bc, double:2.52626E-319)
            r5[r6] = r8
            r6 = 64
            r8 = 18628(0x48c4, double:9.2035E-320)
            r5[r6] = r8
            r6 = 65
            r8 = 22757(0x58e5, double:1.12435E-319)
            r5[r6] = r8
            r6 = 66
            r8 = 26758(0x6886, double:1.322E-319)
            r5[r6] = r8
            r6 = 67
            r8 = 30887(0x78a7, double:1.526E-319)
            r5[r6] = r8
            r6 = 68
            r8 = 2112(0x840, double:1.0435E-320)
            r5[r6] = r8
            r6 = 69
            r8 = 6241(0x1861, double:3.0835E-320)
            r5[r6] = r8
            r6 = 70
            r8 = 10242(0x2802, double:5.06E-320)
            r5[r6] = r8
            r6 = 71
            r8 = 14371(0x3823, double:7.1E-320)
            r5[r6] = r8
            r6 = 72
            r8 = 51660(0xc9cc, double:2.55234E-319)
            r5[r6] = r8
            r6 = 73
            r8 = 55789(0xd9ed, double:2.75634E-319)
            r5[r6] = r8
            r6 = 74
            r8 = 59790(0xe98e, double:2.954E-319)
            r5[r6] = r8
            r6 = 75
            r8 = 63919(0xf9af, double:3.158E-319)
            r5[r6] = r8
            r6 = 76
            r8 = 35144(0x8948, double:1.73634E-319)
            r5[r6] = r8
            r6 = 77
            r8 = 39273(0x9969, double:1.94034E-319)
            r5[r6] = r8
            r6 = 78
            r8 = 43274(0xa90a, double:2.138E-319)
            r5[r6] = r8
            r6 = 79
            r8 = 47403(0xb92b, double:2.342E-319)
            r5[r6] = r8
            r6 = 80
            r8 = 23285(0x5af5, double:1.15043E-319)
            r5[r6] = r8
            r6 = 81
            r8 = 19156(0x4ad4, double:9.4643E-320)
            r5[r6] = r8
            r6 = 82
            r8 = 31415(0x7ab7, double:1.5521E-319)
            r5[r6] = r8
            r6 = 83
            r8 = 27286(0x6a96, double:1.3481E-319)
            r5[r6] = r8
            r6 = 84
            r8 = 6769(0x1a71, double:3.3443E-320)
            r5[r6] = r8
            r6 = 85
            r8 = 2640(0xa50, double:1.3043E-320)
            r5[r6] = r8
            r6 = 86
            r8 = 14899(0x3a33, double:7.361E-320)
            r5[r6] = r8
            r6 = 87
            r8 = 10770(0x2a12, double:5.321E-320)
            r5[r6] = r8
            r6 = 88
            r8 = 56317(0xdbfd, double:2.78243E-319)
            r5[r6] = r8
            r6 = 89
            r8 = 52188(0xcbdc, double:2.57843E-319)
            r5[r6] = r8
            r6 = 90
            r8 = 64447(0xfbbf, double:3.1841E-319)
            r5[r6] = r8
            r6 = 91
            r8 = 60318(0xeb9e, double:2.9801E-319)
            r5[r6] = r8
            r6 = 92
            r8 = 39801(0x9b79, double:1.96643E-319)
            r5[r6] = r8
            r6 = 93
            r8 = 35672(0x8b58, double:1.76243E-319)
            r5[r6] = r8
            r6 = 94
            r8 = 47931(0xbb3b, double:2.3681E-319)
            r5[r6] = r8
            r6 = 95
            r8 = 43802(0xab1a, double:2.1641E-319)
            r5[r6] = r8
            r6 = 96
            r8 = 27814(0x6ca6, double:1.3742E-319)
            r5[r6] = r8
            r6 = 97
            r8 = 31879(0x7c87, double:1.57503E-319)
            r5[r6] = r8
            r6 = 98
            r8 = 19684(0x4ce4, double:9.725E-320)
            r5[r6] = r8
            r6 = 99
            r8 = 23749(0x5cc5, double:1.17336E-319)
            r5[r6] = r8
            r6 = 100
            r8 = 11298(0x2c22, double:5.582E-320)
            r5[r6] = r8
            r6 = 101(0x65, float:1.42E-43)
            r8 = 15363(0x3c03, double:7.5903E-320)
            r5[r6] = r8
            r6 = 102(0x66, float:1.43E-43)
            r8 = 3168(0xc60, double:1.565E-320)
            r5[r6] = r8
            r6 = 103(0x67, float:1.44E-43)
            r8 = 7233(0x1c41, double:3.5736E-320)
            r5[r6] = r8
            r6 = 104(0x68, float:1.46E-43)
            r8 = 60846(0xedae, double:3.0062E-319)
            r5[r6] = r8
            r6 = 105(0x69, float:1.47E-43)
            r8 = 64911(0xfd8f, double:3.20703E-319)
            r5[r6] = r8
            r6 = 106(0x6a, float:1.49E-43)
            r8 = 52716(0xcdec, double:2.6045E-319)
            r5[r6] = r8
            r6 = 107(0x6b, float:1.5E-43)
            r8 = 56781(0xddcd, double:2.80535E-319)
            r5[r6] = r8
            r6 = 108(0x6c, float:1.51E-43)
            r8 = 44330(0xad2a, double:2.1902E-319)
            r5[r6] = r8
            r6 = 109(0x6d, float:1.53E-43)
            r8 = 48395(0xbd0b, double:2.39103E-319)
            r5[r6] = r8
            r6 = 110(0x6e, float:1.54E-43)
            r8 = 36200(0x8d68, double:1.7885E-319)
            r5[r6] = r8
            r6 = 111(0x6f, float:1.56E-43)
            r8 = 40265(0x9d49, double:1.98936E-319)
            r5[r6] = r8
            r6 = 112(0x70, float:1.57E-43)
            r8 = 32407(0x7e97, double:1.6011E-319)
            r5[r6] = r8
            r6 = 113(0x71, float:1.58E-43)
            r8 = 28342(0x6eb6, double:1.4003E-319)
            r5[r6] = r8
            r6 = 114(0x72, float:1.6E-43)
            r8 = 24277(0x5ed5, double:1.19944E-319)
            r5[r6] = r8
            r6 = 115(0x73, float:1.61E-43)
            r8 = 20212(0x4ef4, double:9.986E-320)
            r5[r6] = r8
            r6 = 116(0x74, float:1.63E-43)
            r8 = 15891(0x3e13, double:7.851E-320)
            r5[r6] = r8
            r6 = 117(0x75, float:1.64E-43)
            r8 = 11826(0x2e32, double:5.843E-320)
            r5[r6] = r8
            r6 = 118(0x76, float:1.65E-43)
            r8 = 7761(0x1e51, double:3.8344E-320)
            r5[r6] = r8
            r6 = 119(0x77, float:1.67E-43)
            r8 = 3696(0xe70, double:1.826E-320)
            r5[r6] = r8
            r6 = 120(0x78, float:1.68E-43)
            r8 = 65439(0xff9f, double:3.2331E-319)
            r5[r6] = r8
            r6 = 121(0x79, float:1.7E-43)
            r8 = 61374(0xefbe, double:3.0323E-319)
            r5[r6] = r8
            r6 = 122(0x7a, float:1.71E-43)
            r8 = 57309(0xdfdd, double:2.83144E-319)
            r5[r6] = r8
            r6 = 123(0x7b, float:1.72E-43)
            r8 = 53244(0xcffc, double:2.6306E-319)
            r5[r6] = r8
            r6 = 124(0x7c, float:1.74E-43)
            r8 = 48923(0xbf1b, double:2.4171E-319)
            r5[r6] = r8
            r6 = 125(0x7d, float:1.75E-43)
            r8 = 44858(0xaf3a, double:2.2163E-319)
            r5[r6] = r8
            r6 = 126(0x7e, float:1.77E-43)
            r8 = 40793(0x9f59, double:2.01544E-319)
            r5[r6] = r8
            r6 = 127(0x7f, float:1.78E-43)
            r8 = 36728(0x8f78, double:1.8146E-319)
            r5[r6] = r8
            r6 = 128(0x80, float:1.794E-43)
            r8 = 37256(0x9188, double:1.8407E-319)
            r5[r6] = r8
            r6 = 129(0x81, float:1.81E-43)
            r8 = 33193(0x81a9, double:1.63995E-319)
            r5[r6] = r8
            r6 = 130(0x82, float:1.82E-43)
            r8 = 45514(0xb1ca, double:2.2487E-319)
            r5[r6] = r8
            r6 = 131(0x83, float:1.84E-43)
            r8 = 41451(0xa1eb, double:2.04795E-319)
            r5[r6] = r8
            r6 = 132(0x84, float:1.85E-43)
            r8 = 53516(0xd10c, double:2.64404E-319)
            r5[r6] = r8
            r6 = 133(0x85, float:1.86E-43)
            r8 = 49453(0xc12d, double:2.4433E-319)
            r5[r6] = r8
            r6 = 134(0x86, float:1.88E-43)
            r8 = 61774(0xf14e, double:3.05204E-319)
            r5[r6] = r8
            r6 = 135(0x87, float:1.89E-43)
            r8 = 57711(0xe16f, double:2.8513E-319)
            r5[r6] = r8
            r6 = 136(0x88, float:1.9E-43)
            r8 = 4224(0x1080, double:2.087E-320)
            r5[r6] = r8
            r6 = 137(0x89, float:1.92E-43)
            r8 = 161(0xa1, double:7.95E-322)
            r5[r6] = r8
            r6 = 138(0x8a, float:1.93E-43)
            r8 = 12482(0x30c2, double:6.167E-320)
            r5[r6] = r8
            r6 = 139(0x8b, float:1.95E-43)
            r8 = 8419(0x20e3, double:4.1595E-320)
            r5[r6] = r8
            r6 = 140(0x8c, float:1.96E-43)
            r8 = 20484(0x5004, double:1.01204E-319)
            r5[r6] = r8
            r6 = 141(0x8d, float:1.98E-43)
            r8 = 16421(0x4025, double:8.113E-320)
            r5[r6] = r8
            r6 = 142(0x8e, float:1.99E-43)
            r8 = 28742(0x7046, double:1.42004E-319)
            r5[r6] = r8
            r6 = 143(0x8f, float:2.0E-43)
            r8 = 24679(0x6067, double:1.2193E-319)
            r5[r6] = r8
            r6 = 144(0x90, float:2.02E-43)
            r8 = 33721(0x83b9, double:1.66604E-319)
            r5[r6] = r8
            r6 = 145(0x91, float:2.03E-43)
            r8 = 37784(0x9398, double:1.8668E-319)
            r5[r6] = r8
            r6 = 146(0x92, float:2.05E-43)
            r8 = 41979(0xa3fb, double:2.07404E-319)
            r5[r6] = r8
            r6 = 147(0x93, float:2.06E-43)
            r8 = 46042(0xb3da, double:2.2748E-319)
            r5[r6] = r8
            r6 = 148(0x94, float:2.07E-43)
            r8 = 49981(0xc33d, double:2.4694E-319)
            r5[r6] = r8
            r6 = 149(0x95, float:2.09E-43)
            r8 = 54044(0xd31c, double:2.67013E-319)
            r5[r6] = r8
            r6 = 150(0x96, float:2.1E-43)
            r8 = 58239(0xe37f, double:2.8774E-319)
            r5[r6] = r8
            r6 = 151(0x97, float:2.12E-43)
            r8 = 62302(0xf35e, double:3.07813E-319)
            r5[r6] = r8
            r6 = 152(0x98, float:2.13E-43)
            r8 = 689(0x2b1, double:3.404E-321)
            r5[r6] = r8
            r6 = 153(0x99, float:2.14E-43)
            r8 = 4752(0x1290, double:2.348E-320)
            r5[r6] = r8
            r6 = 154(0x9a, float:2.16E-43)
            r8 = 8947(0x22f3, double:4.4204E-320)
            r5[r6] = r8
            r6 = 155(0x9b, float:2.17E-43)
            r8 = 13010(0x32d2, double:6.428E-320)
            r5[r6] = r8
            r6 = 156(0x9c, float:2.19E-43)
            r8 = 16949(0x4235, double:8.374E-320)
            r5[r6] = r8
            r6 = 157(0x9d, float:2.2E-43)
            r8 = 21012(0x5214, double:1.03813E-319)
            r5[r6] = r8
            r6 = 158(0x9e, float:2.21E-43)
            r8 = 25207(0x6277, double:1.2454E-319)
            r5[r6] = r8
            r6 = 159(0x9f, float:2.23E-43)
            r8 = 29270(0x7256, double:1.44613E-319)
            r5[r6] = r8
            r6 = 160(0xa0, float:2.24E-43)
            r8 = 46570(0xb5ea, double:2.30086E-319)
            r5[r6] = r8
            r6 = 161(0xa1, float:2.26E-43)
            r8 = 42443(0xa5cb, double:2.09696E-319)
            r5[r6] = r8
            r6 = 162(0xa2, float:2.27E-43)
            r8 = 38312(0x95a8, double:1.89286E-319)
            r5[r6] = r8
            r6 = 163(0xa3, float:2.28E-43)
            r8 = 34185(0x8589, double:1.68896E-319)
            r5[r6] = r8
            r6 = 164(0xa4, float:2.3E-43)
            r8 = 62830(0xf56e, double:3.1042E-319)
            r5[r6] = r8
            r6 = 165(0xa5, float:2.31E-43)
            r8 = 58703(0xe54f, double:2.9003E-319)
            r5[r6] = r8
            r6 = 166(0xa6, float:2.33E-43)
            r8 = 54572(0xd52c, double:2.6962E-319)
            r5[r6] = r8
            r6 = 167(0xa7, float:2.34E-43)
            r8 = 50445(0xc50d, double:2.4923E-319)
            r5[r6] = r8
            r6 = 168(0xa8, float:2.35E-43)
            r8 = 13538(0x34e2, double:6.6887E-320)
            r5[r6] = r8
            r6 = 169(0xa9, float:2.37E-43)
            r8 = 9411(0x24c3, double:4.6497E-320)
            r5[r6] = r8
            r6 = 170(0xaa, float:2.38E-43)
            r8 = 5280(0x14a0, double:2.6087E-320)
            r5[r6] = r8
            r6 = 171(0xab, float:2.4E-43)
            r8 = 1153(0x481, double:5.697E-321)
            r5[r6] = r8
            r6 = 172(0xac, float:2.41E-43)
            r8 = 29798(0x7466, double:1.4722E-319)
            r5[r6] = r8
            r6 = 173(0xad, float:2.42E-43)
            r8 = 25671(0x6447, double:1.2683E-319)
            r5[r6] = r8
            r6 = 174(0xae, float:2.44E-43)
            r8 = 21540(0x5424, double:1.0642E-319)
            r5[r6] = r8
            r6 = 175(0xaf, float:2.45E-43)
            r8 = 17413(0x4405, double:8.603E-320)
            r5[r6] = r8
            r6 = 176(0xb0, float:2.47E-43)
            r8 = 42971(0xa7db, double:2.12305E-319)
            r5[r6] = r8
            r6 = 177(0xb1, float:2.48E-43)
            r8 = 47098(0xb7fa, double:2.32695E-319)
            r5[r6] = r8
            r6 = 178(0xb2, float:2.5E-43)
            r8 = 34713(0x8799, double:1.71505E-319)
            r5[r6] = r8
            r6 = 179(0xb3, float:2.51E-43)
            r8 = 38840(0x97b8, double:1.91895E-319)
            r5[r6] = r8
            r6 = 180(0xb4, float:2.52E-43)
            r8 = 59231(0xe75f, double:2.9264E-319)
            r5[r6] = r8
            r6 = 181(0xb5, float:2.54E-43)
            r8 = 63358(0xf77e, double:3.1303E-319)
            r5[r6] = r8
            r6 = 182(0xb6, float:2.55E-43)
            r8 = 50973(0xc71d, double:2.5184E-319)
            r5[r6] = r8
            r6 = 183(0xb7, float:2.56E-43)
            r8 = 55100(0xd73c, double:2.7223E-319)
            r5[r6] = r8
            r6 = 184(0xb8, float:2.58E-43)
            r8 = 9939(0x26d3, double:4.9105E-320)
            r5[r6] = r8
            r6 = 185(0xb9, float:2.59E-43)
            r8 = 14066(0x36f2, double:6.9495E-320)
            r5[r6] = r8
            r6 = 186(0xba, float:2.6E-43)
            r8 = 1681(0x691, double:8.305E-321)
            r5[r6] = r8
            r6 = 187(0xbb, float:2.62E-43)
            r8 = 5808(0x16b0, double:2.8695E-320)
            r5[r6] = r8
            r6 = 188(0xbc, float:2.63E-43)
            r8 = 26199(0x6657, double:1.2944E-319)
            r5[r6] = r8
            r6 = 189(0xbd, float:2.65E-43)
            r8 = 30326(0x7676, double:1.4983E-319)
            r5[r6] = r8
            r6 = 190(0xbe, float:2.66E-43)
            r8 = 17941(0x4615, double:8.864E-320)
            r5[r6] = r8
            r6 = 191(0xbf, float:2.68E-43)
            r8 = 22068(0x5634, double:1.0903E-319)
            r5[r6] = r8
            r6 = 192(0xc0, float:2.69E-43)
            r8 = 55628(0xd94c, double:2.7484E-319)
            r5[r6] = r8
            r6 = 193(0xc1, float:2.7E-43)
            r8 = 51565(0xc96d, double:2.54765E-319)
            r5[r6] = r8
            r6 = 194(0xc2, float:2.72E-43)
            r8 = 63758(0xf90e, double:3.15006E-319)
            r5[r6] = r8
            r6 = 195(0xc3, float:2.73E-43)
            r8 = 59695(0xe92f, double:2.94932E-319)
            r5[r6] = r8
            r6 = 196(0xc4, float:2.75E-43)
            r8 = 39368(0x99c8, double:1.94504E-319)
            r5[r6] = r8
            r6 = 197(0xc5, float:2.76E-43)
            r8 = 35305(0x89e9, double:1.7443E-319)
            r5[r6] = r8
            r6 = 198(0xc6, float:2.77E-43)
            r8 = 47498(0xb98a, double:2.3467E-319)
            r5[r6] = r8
            r6 = 199(0xc7, float:2.79E-43)
            r8 = 43435(0xa9ab, double:2.14597E-319)
            r5[r6] = r8
            r6 = 200(0xc8, float:2.8E-43)
            r8 = 22596(0x5844, double:1.1164E-319)
            r5[r6] = r8
            r6 = 201(0xc9, float:2.82E-43)
            r8 = 18533(0x4865, double:9.1565E-320)
            r5[r6] = r8
            r6 = 202(0xca, float:2.83E-43)
            r8 = 30726(0x7806, double:1.51807E-319)
            r5[r6] = r8
            r6 = 203(0xcb, float:2.84E-43)
            r8 = 26663(0x6827, double:1.31733E-319)
            r5[r6] = r8
            r6 = 204(0xcc, float:2.86E-43)
            r8 = 6336(0x18c0, double:3.1304E-320)
            r5[r6] = r8
            r6 = 205(0xcd, float:2.87E-43)
            r8 = 2273(0x8e1, double:1.123E-320)
            r5[r6] = r8
            r6 = 206(0xce, float:2.89E-43)
            r8 = 14466(0x3882, double:7.147E-320)
            r5[r6] = r8
            r6 = 207(0xcf, float:2.9E-43)
            r8 = 10403(0x28a3, double:5.14E-320)
            r5[r6] = r8
            r6 = 208(0xd0, float:2.91E-43)
            r8 = 52093(0xcb7d, double:2.57374E-319)
            r5[r6] = r8
            r6 = 209(0xd1, float:2.93E-43)
            r8 = 56156(0xdb5c, double:2.77448E-319)
            r5[r6] = r8
            r6 = 210(0xd2, float:2.94E-43)
            r8 = 60223(0xeb3f, double:2.9754E-319)
            r5[r6] = r8
            r6 = 211(0xd3, float:2.96E-43)
            r8 = 64286(0xfb1e, double:3.17615E-319)
            r5[r6] = r8
            r6 = 212(0xd4, float:2.97E-43)
            r8 = 35833(0x8bf9, double:1.7704E-319)
            r5[r6] = r8
            r6 = 213(0xd5, float:2.98E-43)
            r8 = 39896(0x9bd8, double:1.9711E-319)
            r5[r6] = r8
            r6 = 214(0xd6, float:3.0E-43)
            r8 = 43963(0xabbb, double:2.17206E-319)
            r5[r6] = r8
            r6 = 215(0xd7, float:3.01E-43)
            r8 = 48026(0xbb9a, double:2.3728E-319)
            r5[r6] = r8
            r6 = 216(0xd8, float:3.03E-43)
            r8 = 19061(0x4a75, double:9.4174E-320)
            r5[r6] = r8
            r6 = 217(0xd9, float:3.04E-43)
            r8 = 23124(0x5a54, double:1.1425E-319)
            r5[r6] = r8
            r6 = 218(0xda, float:3.05E-43)
            r8 = 27191(0x6a37, double:1.3434E-319)
            r5[r6] = r8
            r6 = 219(0xdb, float:3.07E-43)
            r8 = 31254(0x7a16, double:1.54415E-319)
            r5[r6] = r8
            r6 = 220(0xdc, float:3.08E-43)
            r8 = 2801(0xaf1, double:1.384E-320)
            r5[r6] = r8
            r6 = 221(0xdd, float:3.1E-43)
            r8 = 6864(0x1ad0, double:3.3913E-320)
            r5[r6] = r8
            r6 = 222(0xde, float:3.11E-43)
            r8 = 10931(0x2ab3, double:5.4006E-320)
            r5[r6] = r8
            r6 = 223(0xdf, float:3.12E-43)
            r8 = 14994(0x3a92, double:7.408E-320)
            r5[r6] = r8
            r6 = 224(0xe0, float:3.14E-43)
            r8 = 64814(0xfd2e, double:3.20224E-319)
            r5[r6] = r8
            r6 = 225(0xe1, float:3.15E-43)
            r8 = 60687(0xed0f, double:2.99834E-319)
            r5[r6] = r8
            r6 = 226(0xe2, float:3.17E-43)
            r8 = 56684(0xdd6c, double:2.80056E-319)
            r5[r6] = r8
            r6 = 227(0xe3, float:3.18E-43)
            r8 = 52557(0xcd4d, double:2.59666E-319)
            r5[r6] = r8
            r6 = 228(0xe4, float:3.2E-43)
            r8 = 48554(0xbdaa, double:2.3989E-319)
            r5[r6] = r8
            r6 = 229(0xe5, float:3.21E-43)
            r8 = 44427(0xad8b, double:2.195E-319)
            r5[r6] = r8
            r6 = 230(0xe6, float:3.22E-43)
            r8 = 40424(0x9de8, double:1.9972E-319)
            r5[r6] = r8
            r6 = 231(0xe7, float:3.24E-43)
            r8 = 36297(0x8dc9, double:1.7933E-319)
            r5[r6] = r8
            r6 = 232(0xe8, float:3.25E-43)
            r8 = 31782(0x7c26, double:1.57024E-319)
            r5[r6] = r8
            r6 = 233(0xe9, float:3.27E-43)
            r8 = 27655(0x6c07, double:1.36634E-319)
            r5[r6] = r8
            r6 = 234(0xea, float:3.28E-43)
            r8 = 23652(0x5c64, double:1.16856E-319)
            r5[r6] = r8
            r6 = 235(0xeb, float:3.3E-43)
            r8 = 19525(0x4c45, double:9.6466E-320)
            r5[r6] = r8
            r6 = 236(0xec, float:3.31E-43)
            r8 = 15522(0x3ca2, double:7.669E-320)
            r5[r6] = r8
            r6 = 237(0xed, float:3.32E-43)
            r8 = 11395(0x2c83, double:5.63E-320)
            r5[r6] = r8
            r6 = 238(0xee, float:3.34E-43)
            r8 = 7392(0x1ce0, double:3.652E-320)
            r5[r6] = r8
            r6 = 239(0xef, float:3.35E-43)
            r8 = 3265(0xcc1, double:1.613E-320)
            r5[r6] = r8
            r6 = 240(0xf0, float:3.36E-43)
            r8 = 61215(0xef1f, double:3.0244E-319)
            r5[r6] = r8
            r6 = 241(0xf1, float:3.38E-43)
            r8 = 65342(0xff3e, double:3.2283E-319)
            r5[r6] = r8
            r6 = 242(0xf2, float:3.39E-43)
            r8 = 53085(0xcf5d, double:2.62275E-319)
            r5[r6] = r8
            r6 = 243(0xf3, float:3.4E-43)
            r8 = 57212(0xdf7c, double:2.82665E-319)
            r5[r6] = r8
            r6 = 244(0xf4, float:3.42E-43)
            r8 = 44955(0xaf9b, double:2.22107E-319)
            r5[r6] = r8
            r6 = 245(0xf5, float:3.43E-43)
            r8 = 49082(0xbfba, double:2.42497E-319)
            r5[r6] = r8
            r6 = 246(0xf6, float:3.45E-43)
            r8 = 36825(0x8fd9, double:1.8194E-319)
            r5[r6] = r8
            r6 = 247(0xf7, float:3.46E-43)
            r8 = 40952(0x9ff8, double:2.0233E-319)
            r5[r6] = r8
            r6 = 248(0xf8, float:3.48E-43)
            r8 = 28183(0x6e17, double:1.39243E-319)
            r5[r6] = r8
            r6 = 249(0xf9, float:3.49E-43)
            r8 = 32310(0x7e36, double:1.59633E-319)
            r5[r6] = r8
            r6 = 250(0xfa, float:3.5E-43)
            r8 = 20053(0x4e55, double:9.9075E-320)
            r5[r6] = r8
            r6 = 251(0xfb, float:3.52E-43)
            r8 = 24180(0x5e74, double:1.19465E-319)
            r5[r6] = r8
            r6 = 252(0xfc, float:3.53E-43)
            r8 = 11923(0x2e93, double:5.8907E-320)
            r5[r6] = r8
            r6 = 253(0xfd, float:3.55E-43)
            r8 = 16050(0x3eb2, double:7.93E-320)
            r5[r6] = r8
            r6 = 254(0xfe, float:3.56E-43)
            r8 = 3793(0xed1, double:1.874E-320)
            r5[r6] = r8
            r6 = 255(0xff, float:3.57E-43)
            r8 = 7920(0x1ef0, double:3.913E-320)
            r5[r6] = r8
            r13.crc_16tab = r5
            r13.mContext = r14
            android.content.Intent r5 = new android.content.Intent
            java.lang.String r6 = "com.android.example.USB_PERMISSION"
            r5.<init>(r6)
            android.app.PendingIntent r5 = android.app.PendingIntent.getBroadcast(r14, r10, r5, r10)
            r13.mPermissionIntent = r5
            java.lang.String r5 = "usb"
            java.lang.Object r5 = r14.getSystemService(r5)
            android.hardware.usb.UsbManager r5 = (android.hardware.usb.UsbManager) r5
            mUsbManager = r5
            android.hardware.usb.UsbManager r5 = mUsbManager
            if (r5 != 0) goto L_0x06bb
            java.lang.String r5 = "idcard demo"
            java.lang.String r6 = "getSystemService mUsbManager is null"
            android.util.Log.d(r5, r6)
        L_0x06bb:
            android.hardware.usb.UsbManager r5 = mUsbManager
            java.util.HashMap r0 = r5.getDeviceList()
            java.util.Collection r5 = r0.values()
            java.util.Iterator r1 = r5.iterator()
        L_0x06c9:
            boolean r5 = r1.hasNext()
            if (r5 != 0) goto L_0x06f5
        L_0x06cf:
            android.hardware.usb.UsbManager r5 = mUsbManager
            if (r5 == 0) goto L_0x06d7
            android.hardware.usb.UsbDevice r5 = idcard_reader
            if (r5 != 0) goto L_0x074a
        L_0x06d7:
            android.hardware.usb.UsbManager r5 = mUsbManager
            if (r5 != 0) goto L_0x06e2
            java.lang.String r5 = "idcard demo"
            java.lang.String r6 = "musbManager is null"
            android.util.Log.d(r5, r6)
        L_0x06e2:
            android.hardware.usb.UsbDevice r5 = idcard_reader
            if (r5 != 0) goto L_0x06ed
            java.lang.String r5 = "idcard demo"
            java.lang.String r6 = "idcard_reader is null"
            android.util.Log.d(r5, r6)
        L_0x06ed:
            com.telpo.tps550.api.idcard.IdCardInitFailException r5 = new com.telpo.tps550.api.idcard.IdCardInitFailException
            java.lang.String r6 = "Failed to open usb device"
            r5.<init>((java.lang.String) r6)
            throw r5
        L_0x06f5:
            java.lang.Object r3 = r1.next()
            android.hardware.usb.UsbDevice r3 = (android.hardware.usb.UsbDevice) r3
            int r2 = r3.getProductId()
            int r4 = r3.getVendorId()
            java.lang.String r5 = "idcard demo"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            java.lang.String r7 = "pid:"
            r6.<init>(r7)
            java.lang.StringBuilder r6 = r6.append(r2)
            java.lang.String r7 = ";vid:"
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.StringBuilder r6 = r6.append(r4)
            java.lang.String r6 = r6.toString()
            android.util.Log.d(r5, r6)
            r5 = 50010(0xc35a, float:7.0079E-41)
            if (r2 != r5) goto L_0x072a
            r5 = 1024(0x400, float:1.435E-42)
            if (r4 == r5) goto L_0x0738
        L_0x072a:
            r5 = 22352(0x5750, float:3.1322E-41)
            if (r2 != r5) goto L_0x0732
            r5 = 1155(0x483, float:1.618E-42)
            if (r4 == r5) goto L_0x0738
        L_0x0732:
            if (r2 != r12) goto L_0x06c9
            r5 = 10473(0x28e9, float:1.4676E-41)
            if (r4 != r5) goto L_0x06c9
        L_0x0738:
            idcard_reader = r3
            android.hardware.usb.UsbManager r5 = mUsbManager
            boolean r5 = r5.hasPermission(r3)
            if (r5 != 0) goto L_0x06cf
            android.hardware.usb.UsbManager r5 = mUsbManager
            android.app.PendingIntent r6 = r13.mPermissionIntent
            r5.requestPermission(r3, r6)
            goto L_0x06c9
        L_0x074a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.idcard.UsbIdCard.<init>(android.content.Context):void");
    }

    public synchronized IdentityMsg checkIdCard() throws TelpoException {
        byte[] cmd_read_card;
        byte[] bArr;
        byte[] cmd_find_card = new byte[10];
        cmd_find_card[0] = -86;
        cmd_find_card[1] = -86;
        cmd_find_card[2] = -86;
        cmd_find_card[3] = -106;
        cmd_find_card[4] = 105;
        cmd_find_card[6] = 3;
        cmd_find_card[7] = 32;
        cmd_find_card[8] = 1;
        cmd_find_card[9] = 34;
        byte[] bArr2 = new byte[3];
        bArr2[2] = -97;
        byte[] requestUSBDataBtn = requestUSBDataBtn(cmd_find_card, bArr2);
        byte[] cmd_select_card = new byte[10];
        cmd_select_card[0] = -86;
        cmd_select_card[1] = -86;
        cmd_select_card[2] = -86;
        cmd_select_card[3] = -106;
        cmd_select_card[4] = 105;
        cmd_select_card[6] = 3;
        cmd_select_card[7] = 32;
        cmd_select_card[8] = 2;
        cmd_select_card[9] = 33;
        byte[] bArr3 = new byte[3];
        bArr3[2] = -112;
        byte[] ret = requestUSBDataBtn(cmd_select_card, bArr3);
        cmd_read_card = new byte[10];
        cmd_read_card[0] = -86;
        cmd_read_card[1] = -86;
        cmd_read_card[2] = -86;
        cmd_read_card[3] = -106;
        cmd_read_card[4] = 105;
        cmd_read_card[6] = 3;
        cmd_read_card[7] = 48;
        cmd_read_card[8] = 16;
        cmd_read_card[9] = 35;
        bArr = new byte[3];
        bArr[2] = -112;
        return decodeIdCardBaseInfo(requestUSBDataBtn(cmd_read_card, bArr));
    }

    public synchronized byte[] findCard() throws TelpoException {
        byte[] ret;
        byte[] cmd_find_card = new byte[10];
        cmd_find_card[0] = -86;
        cmd_find_card[1] = -86;
        cmd_find_card[2] = -86;
        cmd_find_card[3] = -106;
        cmd_find_card[4] = 105;
        cmd_find_card[6] = 3;
        cmd_find_card[7] = 32;
        cmd_find_card[8] = 1;
        cmd_find_card[9] = 34;
        byte[] bArr = new byte[3];
        bArr[2] = -97;
        ret = requestUSBDataBtn(cmd_find_card, bArr, 1);
        if (ret == null) {
            ret = null;
        }
        return ret;
    }

    public synchronized byte[] selectCard() throws TelpoException {
        byte[] ret;
        byte[] cmd_select_card = new byte[10];
        cmd_select_card[0] = -86;
        cmd_select_card[1] = -86;
        cmd_select_card[2] = -86;
        cmd_select_card[3] = -106;
        cmd_select_card[4] = 105;
        cmd_select_card[6] = 3;
        cmd_select_card[7] = 32;
        cmd_select_card[8] = 2;
        cmd_select_card[9] = 33;
        byte[] bArr = new byte[3];
        bArr[2] = -112;
        ret = requestUSBDataBtn(cmd_select_card, bArr, 1);
        if (ret == null) {
            ret = null;
        }
        return ret;
    }

    public synchronized byte[] readCard() throws TelpoException {
        byte[] ret;
        byte[] cmd_read_card2 = new byte[10];
        cmd_read_card2[0] = -86;
        cmd_read_card2[1] = -86;
        cmd_read_card2[2] = -86;
        cmd_read_card2[3] = -106;
        cmd_read_card2[4] = 105;
        cmd_read_card2[6] = 3;
        cmd_read_card2[7] = 48;
        cmd_read_card2[8] = 16;
        cmd_read_card2[9] = 35;
        byte[] bArr = new byte[3];
        bArr[2] = -112;
        ret = requestUSBDataBtn(cmd_read_card2, bArr);
        if (ret == null) {
            ret = null;
        }
        return ret;
    }

    public static synchronized byte[] getIdCardImage(IdentityMsg info) throws TelpoException {
        byte[] image;
        synchronized (UsbIdCard.class) {
            image = info.getHead_photo();
            if (image == null) {
                throw new IdCardNotCheckException();
            }
        }
        return image;
    }

    public static synchronized byte[] getFringerPrint(IdentityMsg info) throws TelpoException {
        byte[] fringerprint;
        synchronized (UsbIdCard.class) {
            byte[] image = getIdCardImage(info);
            if (image == null) {
                throw new IdCardNotCheckException();
            }
            try {
                fringerprint = Arrays.copyOfRange(image, imageDatalength, image.length);
                if (fringerprint == null) {
                    throw new IdCardNotCheckException();
                }
            } catch (Exception e) {
                throw new IdCardNotCheckException();
            }
        }
        return fringerprint;
    }

    public static Bitmap decodeIdCardImage(byte[] image) throws TelpoException {
        if (image == null) {
            throw new ImageDecodeException();
        }
        byte[] buf = new byte[WLTService.imgLength];
        if (1 == WLTService.wlt2Bmp(image, buf)) {
            return IDPhotoHelper.Bgr2Bitmap(buf);
        }
        throw new ImageDecodeException();
    }

    public synchronized String getSAM() throws TelpoException {
        String str;
        byte[] cmd = new byte[10];
        cmd[0] = -86;
        cmd[1] = -86;
        cmd[2] = -86;
        cmd[3] = -106;
        cmd[4] = 105;
        cmd[6] = 3;
        cmd[7] = 18;
        cmd[8] = -1;
        cmd[9] = -18;
        byte[] bArr = new byte[3];
        bArr[2] = -112;
        byte[] sam_info = requestUSBDataBtn(cmd, bArr);
        if (sam_info == null) {
            str = null;
        } else if (sam_info.length == 16) {
            str = bytearray2Str(sam_info, 0, 2, 2) + bytearray2Str(sam_info, 2, 2, 2) + bytearray2Str(sam_info, 4, 4, 8) + bytearray2Str(sam_info, 8, 4, 10) + bytearray2Str(sam_info, 12, 4, 10);
        } else {
            str = null;
        }
        return str;
    }

    public synchronized byte[] getPhyAddr() throws TelpoException {
        byte[] addr;
        byte[] cmd = new byte[5];
        cmd[1] = 54;
        cmd[4] = 8;
        addr = requestAddr(cmd);
        if (addr == null) {
            addr = null;
        }
        return addr;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r1v2, types: [byte] */
    /* JADX WARNING: Incorrect type for immutable var: ssa=byte, code=int, for r2v2, types: [byte] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getVersion() {
        /*
            r6 = this;
            r4 = 7
            byte[] r0 = new byte[r4]
            r0 = {102, 1, 2, 3, -1, 107, 22} // fill-array
            byte[] r3 = r6.requestVersion(r0)
            r1 = 0
            r2 = 0
            if (r3 == 0) goto L_0x0014
            r4 = 1
            byte r1 = r3[r4]
            r4 = 0
            byte r2 = r3[r4]
        L_0x0014:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = java.lang.String.valueOf(r1)
            r4.<init>(r5)
            java.lang.String r5 = "."
            java.lang.StringBuilder r4 = r4.append(r5)
            java.lang.StringBuilder r4 = r4.append(r2)
            java.lang.String r4 = r4.toString()
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.idcard.UsbIdCard.getVersion():java.lang.String");
    }

    public byte[] requestVersion(byte[] cmd) {
        if (idcard_reader == null) {
            return null;
        }
        UsbInterface usbInterface = idcard_reader.getInterface(0);
        UsbEndpoint inEndpoint = usbInterface.getEndpoint(0);
        UsbEndpoint outEndpoint = usbInterface.getEndpoint(1);
        UsbDeviceConnection connection = mUsbManager.openDevice(idcard_reader);
        if (connection == null) {
            return null;
        }
        connection.claimInterface(usbInterface, true);
        int bulkTransfer = connection.bulkTransfer(outEndpoint, cmd, cmd.length, 3000);
        byte[] byte2 = new byte[9];
        int bulkTransfer2 = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 3000);
        return Arrays.copyOfRange(byte2, 5, 7);
    }

    public byte[] requestAddr(byte[] cmd) {
        if (idcard_reader == null) {
            return null;
        }
        UsbInterface usbInterface = idcard_reader.getInterface(0);
        UsbEndpoint inEndpoint = usbInterface.getEndpoint(0);
        UsbEndpoint outEndpoint = usbInterface.getEndpoint(1);
        UsbDeviceConnection connection = mUsbManager.openDevice(idcard_reader);
        if (connection == null) {
            return null;
        }
        connection.claimInterface(usbInterface, true);
        int bulkTransfer = connection.bulkTransfer(outEndpoint, cmd, cmd.length, 3000);
        byte[] byte2 = new byte[128];
        int ret = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 3000);
        if (ret != -1) {
            return Arrays.copyOfRange(byte2, 0, ret);
        }
        return null;
    }

    public byte[] requestUSBDataBtn(byte[] cmd, byte[] resp) {
        if (idcard_reader == null || resp.length != 3) {
            return null;
        }
        UsbInterface usbInterface = idcard_reader.getInterface(0);
        UsbEndpoint inEndpoint = usbInterface.getEndpoint(0);
        UsbEndpoint outEndpoint = usbInterface.getEndpoint(1);
        try {
            UsbDeviceConnection connection = mUsbManager.openDevice(idcard_reader);
            if (connection == null) {
                return null;
            }
            connection.claimInterface(usbInterface, true);
            int bulkTransfer = connection.bulkTransfer(outEndpoint, cmd, cmd.length, 3000);
            byte[] byte2 = new byte[5120];
            int ret = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 3000);
            for (int i = 0; i < SAM_HEADER.length; i++) {
                if (byte2[i] != SAM_HEADER[i]) {
                    connection.close();
                    return null;
                }
            }
            for (int i2 = 0; i2 < resp.length; i2++) {
                if (byte2[SAM_HEADER.length + 2 + i2] != resp[i2]) {
                    connection.close();
                    return null;
                }
            }
            connection.close();
            return Arrays.copyOfRange(byte2, SAM_HEADER.length + 5, ret - 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] requestUSBDataBtn(byte[] cmd, byte[] resp, int single) {
        if (idcard_reader == null || resp.length != 3) {
            return null;
        }
        UsbInterface usbInterface = idcard_reader.getInterface(0);
        UsbEndpoint inEndpoint = usbInterface.getEndpoint(0);
        UsbEndpoint outEndpoint = usbInterface.getEndpoint(1);
        UsbDeviceConnection connection = null;
        try {
            connection = mUsbManager.openDevice(idcard_reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (connection == null) {
            return null;
        }
        connection.claimInterface(usbInterface, true);
        int bulkTransfer = connection.bulkTransfer(outEndpoint, cmd, cmd.length, 3000);
        byte[] byte2 = new byte[5120];
        int ret = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 3000);
        for (int i = 0; i < SAM_HEADER.length; i++) {
            if (byte2[i] != SAM_HEADER[i]) {
                connection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < resp.length; i2++) {
            if (byte2[SAM_HEADER.length + 2 + i2] != resp[i2]) {
                connection.close();
                return null;
            }
        }
        connection.close();
        return Arrays.copyOfRange(byte2, 0, ret);
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 255);
        if (s.length() == 1) {
            return "0" + s;
        }
        return s;
    }

    private static String formatDate(String date) {
        return date.substring(0, 4) + "." + date.substring(4, 6) + "." + date.substring(6, 8);
    }

    private static String bytearray2Str(byte[] data, int start, int length, int targetLength) {
        long number = 0;
        if (data.length < start + length) {
            return "";
        }
        for (int i = 1; i <= length; i++) {
            number = (number * 256) + ((long) (data[(start + length) - i] & 255));
        }
        return String.format("%0" + targetLength + "d", new Object[]{Long.valueOf(number)});
    }

    private static int countChinese(String str) {
        int count = 0;
        while (Pattern.compile("[\\u4e00-\\u9fa5]").matcher(str).find()) {
            count++;
        }
        return count;
    }

    public IdentityMsg decodeIdCardBaseInfo(byte[] ret) {
        if (ret == null) {
            return null;
        }
        byte[] dataByte = Arrays.copyOfRange(ret, 6, ret.length);
        imageDatalength = (((char) ret[2]) * 256) + ((char) ret[3]);
        contentLength = (ret[0] << 8) & 65280;
        contentLength += ret[1];
        imageDatalength = (ret[2] << 8) & 65280;
        imageDatalength += ret[3];
        fplength = (ret[4] << 8) & 65280;
        fplength += ret[5];
        IdentityMsg info = new IdentityMsg();
        String stringBuffer = null;
        try {
            stringBuffer = new String(dataByte, "UTF16-LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        info.setName(stringBuffer.substring(0, 60));
        info.setSex(stringBuffer.substring(60, 61));
        info.setNo(stringBuffer.substring(61, 76));
        info.setCountry(stringBuffer.substring(76, 79));
        info.setCn_name(stringBuffer.substring(79, 94));
        info.setPeriod(stringBuffer.substring(94, 110));
        info.setBorn(stringBuffer.substring(110, 118));
        info.setIdcard_version(stringBuffer.substring(118, 120));
        info.setApartment(stringBuffer.substring(120, 124));
        info.setCard_type(stringBuffer.substring(124, 125));
        info.setReserve(stringBuffer.substring(125, 128));
        info.setHead_photo(Arrays.copyOfRange(dataByte, 256, dataByte.length));
        if (!"I".equals(info.getCard_type())) {
            info.setName(stringBuffer.substring(0, 15));
            info.setSex(stringBuffer.substring(15, 16));
            if (!stringBuffer.substring(16, 18).equals("  ")) {
                info.setNation(stringBuffer.substring(16, 18));
            }
            info.setBorn(stringBuffer.substring(18, 26));
            info.setAddress(stringBuffer.substring(26, 61).trim());
            info.setNo(stringBuffer.substring(61, 79).trim());
            info.setApartment(stringBuffer.substring(79, 94).trim());
            info.setPeriod(stringBuffer.substring(94, 110));
            if (!stringBuffer.substring(110, 119).equals("         ")) {
                info.setPassNum(stringBuffer.substring(110, 119));
            }
            if (!stringBuffer.substring(119, 121).equals("  ")) {
                info.setIssuesNum(stringBuffer.substring(119, 121));
            }
            if (!stringBuffer.substring(124, 125).equals(" ")) {
                info.setCardSignal(stringBuffer.substring(124, 125));
            }
            info.setHead_photo(Arrays.copyOfRange(dataByte, 256, dataByte.length));
        }
        StringBuilder builder = new StringBuilder();
        String temp = info.getName().trim();
        if (countChinese(temp) != 0) {
            if (temp.length() <= 4) {
                for (char c : temp.toCharArray()) {
                    builder.append(c);
                    builder.append(" ");
                }
            } else if (temp.length() > 14) {
                builder.append(temp.substring(0, 14));
                builder.append("\n\t\t\t");
                builder.append(temp.substring(14));
            }
            if (!builder.toString().equals("")) {
                info.setName(builder.toString());
            }
        } else {
            int splitIndex = 26;
            if (temp.length() > 26) {
                String t = temp.substring(0, 26);
                int d1 = t.lastIndexOf(" ");
                int d2 = t.lastIndexOf(",");
                if (!(d1 == -1 && d2 == -1)) {
                    splitIndex = d1 > d2 ? d1 : d2;
                }
                builder.append(temp.substring(0, splitIndex + 1));
                builder.append("\n\t\t\t");
                builder.append(temp.substring(splitIndex + 1));
                info.setName(builder.toString());
            }
        }
        try {
            info.setNation(nation_list[Integer.parseInt(info.getNation()) - 1]);
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
        }
        String temp2 = info.getSex();
        if ("1".equals(temp2)) {
            info.setSex("男 / M");
        } else if ("2".equals(temp2)) {
            info.setSex("女 / F");
        }
        String temp3 = info.getBorn().trim();
        if (temp3.length() >= 8) {
            info.setBorn(formatDate(temp3));
        }
        String temp4 = info.getPeriod().trim();
        if (temp4.length() >= 16) {
            info.setPeriod(String.valueOf(formatDate(temp4.substring(0, 8))) + " - " + formatDate(temp4.substring(8)));
        }
        if (!"I".equals(info.getCard_type())) {
            return info;
        }
        info.setApartment("公安部/Ministry of Public Security");
        return info;
    }

    private boolean moduleUpdate() {
        if (!connectUsb() || !getUpdate()) {
            return false;
        }
        return true;
    }

    private static void replaceIndex(int index, String res, String str) {
        String res2 = String.valueOf(res.substring(0, index)) + str + res.substring(index + 1);
    }

    private static boolean isFileExists(String filePath) {
        try {
            if (!new File(filePath).exists()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean copyFile(String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            File oFile = new File(fileName);
            if (!oFile.exists() || !oFile.canRead()) {
                return false;
            }
            InputStream is = new FileInputStream(oFile);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[IdCard.READER_VID_BIG];
            while (true) {
                int i = is.read(temp);
                if (i <= 0) {
                    break;
                }
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
            boolean z = copyIsFinish;
            return copyIsFinish;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean getUpdate() {
        requestOrCheck(0);
        sleep();
        while (this.checkCount < 5 && !downloadFile()) {
            sleep();
            requestOrCheck(1);
            this.checkCount++;
            sleep();
        }
        if (this.finalRet) {
            return true;
        }
        return false;
    }

    private void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean connectUsb() {
        return connectUsb(-1, -1);
    }

    private boolean connectUsb(int newPID, int newVID) {
        boolean connectedUsb = false;
        this.mPermissionIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mUsbManager = (UsbManager) this.mContext.getSystemService("usb");
        for (UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            int pid = usbDevice.getProductId();
            int vid = usbDevice.getVendorId();
            if ((pid == 50010 && vid == 1024) || ((pid == 22352 && vid == 1155) || ((pid == 650 && vid == 10473) || (pid == newPID && vid == newVID)))) {
                updater = usbDevice;
                if (mUsbManager.hasPermission(usbDevice)) {
                    return true;
                }
                mUsbManager.requestPermission(usbDevice, this.mPermissionIntent);
                connectedUsb = true;
            }
        }
        return connectedUsb;
    }

    private boolean requestUpdateDataBtn(byte[] cmd, int download) {
        if (updater != null) {
            UsbInterface usbInterface = updater.getInterface(0);
            UsbEndpoint inEndpoint = usbInterface.getEndpoint(0);
            UsbEndpoint outEndpoint = usbInterface.getEndpoint(1);
            try {
                UsbDeviceConnection connection = mUsbManager.openDevice(updater);
                if (connection == null) {
                    return false;
                }
                connection.claimInterface(usbInterface, true);
                int bulkTransfer = connection.bulkTransfer(outEndpoint, cmd, cmd.length, 3000);
                byte[] byte2 = new byte[5120];
                int ret = connection.bulkTransfer(inEndpoint, byte2, byte2.length, 3000);
                connection.close();
                this.resultCommand = null;
                if (ret != -1) {
                    this.resultCommand = Arrays.copyOfRange(byte2, 0, ret);
                }
                if (download == 0) {
                    return true;
                }
                if (download == 1) {
                    if (ret == -1 || this.resultCommand[9] != 1) {
                        return false;
                    }
                    return true;
                } else if (download == 3 && ret != -1 && this.resultCommand[9] == 0) {
                    this.finalRet = true;
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private boolean requestOrCheck(int status) {
        if (status == 0) {
            connectUsb();
        } else if (status == 1) {
            connectUsb(IdCard.READER_PID_WINDOWS, IdCard.READER_VID_WINDOWS);
        }
        byte[] project_bin = null;
        try {
            project_bin = getBytes4File(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + "/project.bin");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String codeLenth = Integer.toHexString(project_bin.length);
        if (codeLenth.length() == 4) {
            codeLenth = "0000" + codeLenth;
        } else if (codeLenth.length() == 6) {
            codeLenth = "00" + codeLenth;
        }
        int codeSize = codeLenth.length();
        String codeLenth2 = String.valueOf(codeLenth.substring(codeSize - 2, codeSize)) + codeLenth.substring(codeSize - 4, codeSize - 2) + codeLenth.substring(codeSize - 6, codeSize - 4) + codeLenth.substring(0, codeSize - 6);
        String tempCRC = Long.toHexString(getCRC(project_bin, project_bin.length));
        String crc = String.valueOf(tempCRC.substring(tempCRC.length() - 2, tempCRC.length())) + tempCRC.substring(tempCRC.length() - 4, tempCRC.length() - 2);
        byte xorByte = getXor(hexStringToBytes("00078020" + codeLenth2 + crc));
        return requestUpdateDataBtn(hexStringToBytes("AAAAAA966900078020" + codeLenth2 + crc + StringUtil.toHexString(new byte[]{xorByte})), status);
    }

    private boolean downloadFile() {
        if (!connectUsb(IdCard.READER_PID_WINDOWS, IdCard.READER_VID_WINDOWS)) {
            return false;
        }
        byte[] project_bin = null;
        byte[] project_bin_1 = null;
        byte[] project_bin_2 = null;
        try {
            project_bin = getBytes4File(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + "/project.bin");
            project_bin_1 = Arrays.copyOfRange(project_bin, 0, 16385);
            project_bin_2 = Arrays.copyOfRange(project_bin, 16384, project_bin.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (project_bin == null) {
            return false;
        }
        requestUpdateDataBtn(project_bin_1, 2);
        return requestUpdateDataBtn(project_bin_2, 3);
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x008e  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0098  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] getBytes4File(java.lang.String r17) throws java.io.IOException {
        /*
            r13 = 0
            r6 = 0
            r8 = 0
            r3 = 0
            r10 = 0
            r1 = 0
            java.io.FileInputStream r14 = new java.io.FileInputStream     // Catch:{ Exception -> 0x00b5 }
            r0 = r17
            r14.<init>(r0)     // Catch:{ Exception -> 0x00b5 }
            java.io.BufferedInputStream r7 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x00b7, all -> 0x009c }
            r7.<init>(r14)     // Catch:{ Exception -> 0x00b7, all -> 0x009c }
            java.io.DataInputStream r9 = new java.io.DataInputStream     // Catch:{ Exception -> 0x00ba, all -> 0x009f }
            r9.<init>(r7)     // Catch:{ Exception -> 0x00ba, all -> 0x009f }
            java.io.ByteArrayOutputStream r4 = new java.io.ByteArrayOutputStream     // Catch:{ Exception -> 0x00be, all -> 0x00a3 }
            r4.<init>()     // Catch:{ Exception -> 0x00be, all -> 0x00a3 }
            java.io.DataOutputStream r11 = new java.io.DataOutputStream     // Catch:{ Exception -> 0x00c3, all -> 0x00a8 }
            r11.<init>(r4)     // Catch:{ Exception -> 0x00c3, all -> 0x00a8 }
            r16 = 1024(0x400, float:1.435E-42)
            r0 = r16
            byte[] r5 = new byte[r0]     // Catch:{ Exception -> 0x005b, all -> 0x00ae }
        L_0x0027:
            int r15 = r9.read(r5)     // Catch:{ Exception -> 0x005b, all -> 0x00ae }
            if (r15 >= 0) goto L_0x0053
            byte[] r1 = r4.toByteArray()     // Catch:{ Exception -> 0x005b, all -> 0x00ae }
            if (r14 == 0) goto L_0x0036
            r14.close()
        L_0x0036:
            if (r9 == 0) goto L_0x003b
            r9.close()
        L_0x003b:
            if (r7 == 0) goto L_0x0040
            r7.close()
        L_0x0040:
            if (r4 == 0) goto L_0x0045
            r4.close()
        L_0x0045:
            if (r11 == 0) goto L_0x004a
            r11.close()
        L_0x004a:
            r2 = r1
            r10 = r11
            r3 = r4
            r8 = r9
            r6 = r7
            r13 = r14
            r16 = r1
        L_0x0052:
            return r16
        L_0x0053:
            r16 = 0
            r0 = r16
            r11.write(r5, r0, r15)     // Catch:{ Exception -> 0x005b, all -> 0x00ae }
            goto L_0x0027
        L_0x005b:
            r12 = move-exception
            r10 = r11
            r3 = r4
            r8 = r9
            r6 = r7
            r13 = r14
        L_0x0061:
            r12.printStackTrace()     // Catch:{ all -> 0x0081 }
            if (r13 == 0) goto L_0x0069
            r13.close()
        L_0x0069:
            if (r8 == 0) goto L_0x006e
            r8.close()
        L_0x006e:
            if (r6 == 0) goto L_0x0073
            r6.close()
        L_0x0073:
            if (r3 == 0) goto L_0x0078
            r3.close()
        L_0x0078:
            if (r10 == 0) goto L_0x007d
            r10.close()
        L_0x007d:
            r16 = 0
            r2 = r1
            goto L_0x0052
        L_0x0081:
            r16 = move-exception
        L_0x0082:
            if (r13 == 0) goto L_0x0087
            r13.close()
        L_0x0087:
            if (r8 == 0) goto L_0x008c
            r8.close()
        L_0x008c:
            if (r6 == 0) goto L_0x0091
            r6.close()
        L_0x0091:
            if (r3 == 0) goto L_0x0096
            r3.close()
        L_0x0096:
            if (r10 == 0) goto L_0x009b
            r10.close()
        L_0x009b:
            throw r16
        L_0x009c:
            r16 = move-exception
            r13 = r14
            goto L_0x0082
        L_0x009f:
            r16 = move-exception
            r6 = r7
            r13 = r14
            goto L_0x0082
        L_0x00a3:
            r16 = move-exception
            r8 = r9
            r6 = r7
            r13 = r14
            goto L_0x0082
        L_0x00a8:
            r16 = move-exception
            r3 = r4
            r8 = r9
            r6 = r7
            r13 = r14
            goto L_0x0082
        L_0x00ae:
            r16 = move-exception
            r10 = r11
            r3 = r4
            r8 = r9
            r6 = r7
            r13 = r14
            goto L_0x0082
        L_0x00b5:
            r12 = move-exception
            goto L_0x0061
        L_0x00b7:
            r12 = move-exception
            r13 = r14
            goto L_0x0061
        L_0x00ba:
            r12 = move-exception
            r6 = r7
            r13 = r14
            goto L_0x0061
        L_0x00be:
            r12 = move-exception
            r8 = r9
            r6 = r7
            r13 = r14
            goto L_0x0061
        L_0x00c3:
            r12 = move-exception
            r3 = r4
            r8 = r9
            r6 = r7
            r13 = r14
            goto L_0x0061
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.idcard.UsbIdCard.getBytes4File(java.lang.String):byte[]");
    }

    private static byte getXor(byte[] datas) {
        byte temp = datas[0];
        for (int i = 1; i < datas.length; i++) {
            temp = (byte) (datas[i] ^ temp);
        }
        return temp;
    }

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        String hexString2 = hexString.toUpperCase().replace(" ", "");
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

    private long getCRC(byte[] project, int length) {
        long cksum = 0;
        for (int i = 0; i < length; i++) {
            cksum = this.crc_16tab[(int) (((cksum >> 8) ^ ((long) project[i])) & 255)] ^ (cksum << 8);
        }
        return cksum;
    }
}
