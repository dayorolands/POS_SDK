package com.telpo.tps550.api.idcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;
import android_serialport_api.SerialPort;
import com.telpo.tps550.api.DeviceNotFoundException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.TimeoutException;
import com.telpo.tps550.api.util.ReaderUtils;
import com.telpo.tps550.api.util.ShellUtils;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class IdCard {
    private static final int DEFAULT_BARTRATE = 115200;
    public static final int IDREADER_TYPE_UART = 0;
    public static final int IDREADER_TYPE_USB = 1;
    private static final int PID = 770;
    private static final int PID_110 = 772;
    public static final int READER_PID_BIG = 50010;
    public static final int READER_PID_SMALL = 22352;
    public static final int READER_PID_WINDOWS = 650;
    public static final int READER_VID_BIG = 1024;
    public static final int READER_VID_SMALL = 1155;
    public static final int READER_VID_WINDOWS = 10473;
    private static final int STEP_CHECK = 1;
    private static final int STEP_READ = 3;
    private static final int STEP_SELECT = 2;
    private static final int VID = 6997;
    private static final byte[] byLicData;
    private static int contentLength;
    private static int fplength;
    private static UsbDevice idcard_reader;
    private static int imageDatalength;
    private static IdentityMsg info = null;
    private static UsbIdCard mUsbIdCard = null;
    private static UsbManager mUsbManager;
    private static String[] nation_list = {"汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜", "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳", "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "柯尔克孜", "土", "达斡尔", "仫佬", "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒", "乌孜别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔", "独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺", "其他", "外国血统中国籍人士"};
    private static int now_PID = 0;
    /* access modifiers changed from: private */
    public byte[] idData = null;
    /* access modifiers changed from: private */
    public InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private ReadThread mReadThread;
    private SerialPort serial = null;
    /* access modifiers changed from: private */
    public int step = -1;

    private static native int check_find(int i);

    private static native Object check_idcard(int i, int[] iArr);

    private static native int check_read(int i);

    private static native int check_select(int i);

    private static native boolean connect_idcard(int i, int i2);

    private static native boolean connected_idcard(int i, int i2, String str);

    private static native boolean disconnect_idcard();

    private static native byte[] get_fringerprint();

    private static native byte[] get_image();

    private static native byte[] get_sam();

    static {
        byte[] bArr = new byte[12];
        bArr[0] = 5;
        bArr[2] = 1;
        bArr[4] = 91;
        bArr[5] = 3;
        bArr[6] = 51;
        bArr[7] = 1;
        bArr[8] = 90;
        bArr[9] = -77;
        bArr[10] = 30;
        byLicData = bArr;
        if (SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900.ordinal() && SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            System.loadLibrary("idcard");
        }
    }

    public IdCard() {
    }

    public IdCard(Context context) {
        try {
            mUsbIdCard = new UsbIdCard(context);
        } catch (TelpoException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void open() throws TelpoException {
        synchronized (IdCard.class) {
            File destDir = new File(Environment.getExternalStorageDirectory() + "/wltlib");
            if (!destDir.exists() && !destDir.mkdir()) {
                throw new IdCardInitFailException("Failed to find idcard library directory!");
            } else if (!isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/base.dat") && !copyFile("/system/usr/base.dat", Environment.getExternalStorageDirectory() + "/wltlib/base.dat")) {
                throw new IdCardInitFailException("Failed to find idcard library data file!");
            } else if (!isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/license.lic") && !copyFile("/system/usr/license.lic", Environment.getExternalStorageDirectory() + "/wltlib/license.lic")) {
                throw new IdCardInitFailException("Failed to find idcard library license file!");
            } else if (!connect_idcard(0, DEFAULT_BARTRATE)) {
                throw new DeviceNotFoundException();
            }
        }
    }

    public static synchronized void open(Context context) throws TelpoException {
        synchronized (IdCard.class) {
            if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                TPS900IDCard.open(context);
            } else {
                File destDir = new File(Environment.getExternalStorageDirectory() + "/wltlib");
                if (!destDir.exists()) {
                    destDir.mkdir();
                }
                if (!isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/base.dat")) {
                    copyFile("/system/usr/base.dat", Environment.getExternalStorageDirectory() + "/wltlib/base.dat");
                }
                if (!isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/license.lic")) {
                    copyFile("/system/usr/license.lic", Environment.getExternalStorageDirectory() + "/wltlib/license.lic");
                }
                if (!connect_idcard(0, DEFAULT_BARTRATE)) {
                    throw new DeviceNotFoundException();
                }
            }
        }
    }

    public static synchronized void open(int type, int pautRate) throws TelpoException {
        synchronized (IdCard.class) {
            if (type == 0 || type == 1) {
                File destDir = new File(Environment.getExternalStorageDirectory() + "/wltlib");
                if (!destDir.exists() && !destDir.mkdir()) {
                    throw new IdCardInitFailException("Failed to find idcard library directory!");
                } else if (!isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/base.dat") && !copyFile("/system/usr/base.dat", Environment.getExternalStorageDirectory() + "/wltlib/base.dat")) {
                    throw new IdCardInitFailException("Failed to find idcard library data file!");
                } else if (!isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/license.lic") && !copyFile("/system/usr/license.lic", Environment.getExternalStorageDirectory() + "/wltlib/license.lic")) {
                    throw new IdCardInitFailException("Failed to find idcard library license file!");
                } else if (!connect_idcard(type, pautRate)) {
                    throw new DeviceNotFoundException();
                }
            } else {
                throw new IllegalArgumentException("Idcard reader type is invalid!");
            }
        }
    }

    public static synchronized void open(int pautRate, String uart) throws TelpoException {
        synchronized (IdCard.class) {
            connected_idcard(0, pautRate, uart);
        }
    }

    public static synchronized void open(int type) throws TelpoException {
        synchronized (IdCard.class) {
            if (type == 0 || type == 1) {
                File destDir = new File(Environment.getExternalStorageDirectory() + "/wltlib");
                if (!destDir.exists() && !destDir.mkdir()) {
                    throw new IdCardInitFailException("Failed to find idcard library directory!");
                } else if (!isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/base.dat") && !copyFile("/system/usr/base.dat", Environment.getExternalStorageDirectory() + "/wltlib/base.dat")) {
                    throw new IdCardInitFailException("Failed to find idcard library data file!");
                } else if (isFileExists(Environment.getExternalStorageDirectory() + "/wltlib/license.lic") || copyFile("/system/usr/license.lic", Environment.getExternalStorageDirectory() + "/wltlib/license.lic")) {
                    if (type == 1) {
                        ShellUtils.CommandResult result = ShellUtils.execCommand("chmod -R 777 /dev/bus/usb/", true);
                        Log.w("result", String.valueOf(result.result) + result.errorMsg);
                    }
                    if (!connect_idcard(type, DEFAULT_BARTRATE)) {
                        throw new DeviceNotFoundException();
                    }
                } else {
                    throw new IdCardInitFailException("Failed to find idcard library license file!");
                }
            } else {
                throw new IllegalArgumentException("Idcard reader type is invalid!");
            }
        }
    }

    public static synchronized int open(int type, Context context) {
        int i = 1;
        synchronized (IdCard.class) {
            if (type == 1) {
                try {
                    mUsbIdCard = new UsbIdCard(context);
                    i = 0;
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            } else {
                i = -99;
            }
        }
        return i;
    }

    public static synchronized IdentityMsg checkIdCard(int timeoutInMs) throws TelpoException {
        IdentityMsg info2;
        synchronized (IdCard.class) {
            if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                info2 = TPS900IDCard.checkIdCard(timeoutInMs);
            } else if (mUsbIdCard != null) {
                info = mUsbIdCard.checkIdCard();
                info2 = info;
            } else {
                int[] result = new int[2];
                info2 = (IdentityMsg) check_idcard(timeoutInMs, result);
                if (info2 != null) {
                    StringBuilder builder = new StringBuilder();
                    String temp = info2.getName().trim();
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
                            info2.setName(builder.toString());
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
                            info2.setName(builder.toString());
                        }
                    }
                    String temp2 = info2.getSex();
                    if ("男".equals(temp2)) {
                        info2.setSex(String.valueOf(temp2) + " / M");
                    } else if ("女".equals(temp2)) {
                        info2.setSex(String.valueOf(temp2) + " / F");
                    }
                    String temp3 = info2.getBorn().trim();
                    if (temp3.length() >= 8) {
                        info2.setBorn(formatDate(temp3));
                    }
                    String temp4 = info2.getPeriod().trim();
                    if (temp4.length() >= 17) {
                        info2.setPeriod(String.valueOf(formatDate(temp4.substring(0, 8))) + " - " + formatDate(temp4.substring(9)));
                    }
                    if ("I".equals(info2.getCard_type())) {
                        info2.setApartment("公安部/Ministry of Public Security");
                    }
                } else if (result[0] == -5) {
                    close();
                    throw new DeviceNotFoundException();
                } else {
                    throw new TimeoutException();
                }
            }
        }
        return info2;
    }

    public IdentityMsg checkIdCardOverseas(int timeoutInMs) throws TelpoException {
        return checkIdCardOverseas((String) null, timeoutInMs);
    }

    public IdentityMsg checkIdCardOverseas(String uart, int timeoutInMs) throws TelpoException {
        if (mUsbIdCard != null) {
            info = mUsbIdCard.checkIdCard();
            return info;
        }
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        checkId(uart);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        while (endTime - startTime < ((long) timeoutInMs)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.step == 3 && this.idData != null && endTime - startTime > 800 && this.idData.length < 50) {
                Log.d("idcard demo", "iddata while length:" + this.idData.length);
                Log.d("idcard demo", "iddata while content" + StringUtil.toHexString(this.idData));
                checkId(uart);
            }
            if (this.idData != null && this.idData.length == 1297 && endTime - startTime > 50) {
                Log.d("idcard demo", "return idData 1297");
                closeStream();
                return decodeIdCardBaseInfo(this.idData);
            } else if (this.idData == null || this.idData.length != 2321) {
                endTime = System.currentTimeMillis();
            } else {
                Log.d("idcard demo", "return idData 2321");
                closeStream();
                return decodeIdCardBaseInfo(this.idData);
            }
        }
        closeStream();
        throw new TimeoutException();
    }

    public static synchronized int serial_idcard_find() {
        int ret;
        synchronized (IdCard.class) {
            ret = check_find(2000);
        }
        return ret;
    }

    public static synchronized int serial_idcard_select() {
        int ret;
        synchronized (IdCard.class) {
            ret = check_select(2000);
        }
        return ret;
    }

    public static synchronized int serial_idcard_read() {
        int ret;
        synchronized (IdCard.class) {
            ret = check_read(2000);
        }
        return ret;
    }

    public static synchronized int usb_idcard_find() {
        int i = 1;
        synchronized (IdCard.class) {
            if (mUsbIdCard != null) {
                try {
                    byte[] findCurrect = new byte[15];
                    findCurrect[0] = -86;
                    findCurrect[1] = -86;
                    findCurrect[2] = -86;
                    findCurrect[3] = -106;
                    findCurrect[4] = 105;
                    findCurrect[6] = 8;
                    findCurrect[9] = -97;
                    findCurrect[14] = -105;
                    byte[] findByte = mUsbIdCard.findCard();
                    if (findByte != null) {
                        if (StringUtil.toHexString(findByte).equals(StringUtil.toHexString(findCurrect))) {
                            i = 0;
                        }
                    }
                } catch (TelpoException e) {
                    e.printStackTrace();
                    i = -99;
                }
            } else {
                i = -99;
            }
        }
        return i;
    }

    public static synchronized int usb_idcard_select() {
        int i = 1;
        synchronized (IdCard.class) {
            if (mUsbIdCard != null) {
                try {
                    byte[] selectCurrect = new byte[19];
                    selectCurrect[0] = -86;
                    selectCurrect[1] = -86;
                    selectCurrect[2] = -86;
                    selectCurrect[3] = -106;
                    selectCurrect[4] = 105;
                    selectCurrect[6] = 12;
                    selectCurrect[9] = -112;
                    selectCurrect[18] = -100;
                    byte[] selectByte = mUsbIdCard.selectCard();
                    if (selectByte != null) {
                        if (StringUtil.toHexString(selectByte).equals(StringUtil.toHexString(selectCurrect))) {
                            i = 0;
                        }
                    }
                } catch (TelpoException e) {
                    e.printStackTrace();
                    i = -99;
                }
            } else {
                i = -99;
            }
        }
        return i;
    }

    public static synchronized byte[] usb_idcard_read() {
        byte[] bArr;
        synchronized (IdCard.class) {
            if (mUsbIdCard != null) {
                try {
                    bArr = mUsbIdCard.readCard();
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
            bArr = null;
        }
        return bArr;
    }

    public static synchronized byte[] getIdCardImage() throws TelpoException {
        byte[] image;
        synchronized (IdCard.class) {
            if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                image = TPS900IDCard.getIdCardImage();
            } else if (mUsbIdCard == null || info == null) {
                image = get_image();
                if (image == null) {
                    throw new IdCardNotCheckException();
                }
            } else {
                image = UsbIdCard.getIdCardImage(info);
            }
        }
        return image;
    }

    public byte[] getIdCardImageOverseas(IdentityMsg info2) throws TelpoException {
        if (mUsbIdCard != null && info2 != null) {
            return UsbIdCard.getIdCardImage(info2);
        }
        byte[] image = info2.getHead_photo();
        if (image != null) {
            return image;
        }
        throw new IdCardNotCheckException();
    }

    public static synchronized byte[] getFringerPrint() throws TelpoException {
        byte[] fringerprint;
        synchronized (IdCard.class) {
            if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                fringerprint = null;
            } else if (mUsbIdCard == null || info == null) {
                fringerprint = get_fringerprint();
                if (fringerprint == null) {
                    throw new IdCardNotCheckException();
                }
            } else {
                fringerprint = UsbIdCard.getFringerPrint(info);
            }
        }
        return fringerprint;
    }

    public byte[] getFringerPrintOverseas(IdentityMsg info2) throws TelpoException {
        if (mUsbIdCard != null && info2 != null) {
            return UsbIdCard.getFringerPrint(info2);
        }
        byte[] image = getIdCardImageOverseas(info2);
        if (image == null) {
            throw new IdCardNotCheckException();
        }
        try {
            byte[] fringerprint = Arrays.copyOfRange(image, imageDatalength, image.length);
            if (fringerprint != null) {
                return fringerprint;
            }
            throw new IdCardNotCheckException();
        } catch (Exception e) {
            throw new IdCardNotCheckException();
        }
    }

    public static Bitmap decodeIdCardImage(byte[] image) throws TelpoException {
        if (image == null) {
            throw new ImageDecodeException();
        } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            return TPS900IDCard.decodeIdCardImage(image);
        } else {
            if (mUsbIdCard != null) {
                return UsbIdCard.decodeIdCardImage(image);
            }
            try {
                return BitmapFactory.decodeFile(String.valueOf(Environment.getExternalStorageDirectory().getAbsolutePath()) + "/wltlib/zp.bmp");
            } catch (Exception e) {
                e.printStackTrace();
                throw new ImageDecodeException();
            }
        }
    }

    public Bitmap decodeIdCardImageOverseas(byte[] image) throws TelpoException {
        if (image == null) {
            throw new ImageDecodeException();
        } else if (mUsbIdCard != null) {
            return UsbIdCard.decodeIdCardImage(image);
        } else {
            byte[] buf = new byte[WLTService.imgLength];
            if (1 == WLTService.wlt2Bmp(image, buf)) {
                return IDPhotoHelper.Bgr2Bitmap(buf);
            }
            throw new ImageDecodeException();
        }
    }

    public static synchronized String getSAM() throws TelpoException {
        String str = null;
        synchronized (IdCard.class) {
            if (!(SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal())) {
                if (mUsbIdCard != null) {
                    str = mUsbIdCard.getSAM();
                } else {
                    byte[] sam_info = get_sam();
                    if (sam_info == null) {
                        throw new IdCardNotReadSnException();
                    } else if (sam_info.length == 16) {
                        str = bytearray2Str(sam_info, 0, 2, 2) + bytearray2Str(sam_info, 2, 2, 2) + bytearray2Str(sam_info, 4, 4, 8) + bytearray2Str(sam_info, 8, 4, 10) + bytearray2Str(sam_info, 12, 4, 10);
                    }
                }
            }
        }
        return str;
    }

    public static synchronized String getVersion() throws TelpoException {
        String str;
        synchronized (IdCard.class) {
            if (mUsbIdCard != null) {
                str = mUsbIdCard.getVersion();
            } else {
                str = null;
            }
        }
        return str;
    }

    public static synchronized byte[] getPhyAddr() throws TelpoException {
        byte[] bArr;
        synchronized (IdCard.class) {
            if (mUsbIdCard != null) {
                bArr = mUsbIdCard.getPhyAddr();
            } else {
                bArr = null;
            }
        }
        return bArr;
    }

    public static int close() {
        try {
            if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
                TPS900IDCard.close();
            } else if (mUsbIdCard != null) {
                usbClose();
            } else {
                disconnect_idcard();
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static void usbClose() {
        if (mUsbIdCard != null) {
            mUsbIdCard = null;
        }
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

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        String hexString2 = hexString.toUpperCase();
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
            byte[] temp = new byte[READER_VID_BIG];
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
        return copyIsFinish;
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

    private IdentityMsg decodeIdCardBaseInfo(byte[] ret) {
        if (ret == null) {
            return null;
        }
        byte[] ret2 = Arrays.copyOfRange(ret, 10, ret.length - 1);
        byte[] dataByte = Arrays.copyOfRange(ret2, 6, ret2.length);
        imageDatalength = (((char) ret2[2]) * 256) + ((char) ret2[3]);
        contentLength = (ret2[0] << 8) & 65280;
        contentLength += ret2[1];
        imageDatalength = (ret2[2] << 8) & 65280;
        imageDatalength += ret2[3];
        fplength = (ret2[4] << 8) & 65280;
        fplength += ret2[5];
        IdentityMsg info2 = new IdentityMsg();
        String stringBuffer = null;
        try {
            stringBuffer = new String(dataByte, "UTF16-LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        info2.setName(stringBuffer.substring(0, 60));
        info2.setSex(stringBuffer.substring(60, 61));
        info2.setNo(stringBuffer.substring(61, 76));
        info2.setCountry(stringBuffer.substring(76, 79));
        info2.setCn_name(stringBuffer.substring(79, 94));
        info2.setPeriod(stringBuffer.substring(94, 110));
        info2.setBorn(stringBuffer.substring(110, 118));
        info2.setIdcard_version(stringBuffer.substring(118, 120));
        info2.setApartment(stringBuffer.substring(120, 124));
        info2.setCard_type(stringBuffer.substring(124, 125));
        info2.setReserve(stringBuffer.substring(125, 128));
        info2.setHead_photo(Arrays.copyOfRange(dataByte, 256, dataByte.length));
        if (!"I".equals(info2.getCard_type())) {
            info2.setName(stringBuffer.substring(0, 15));
            info2.setSex(stringBuffer.substring(15, 16));
            if (!stringBuffer.substring(16, 18).equals("  ")) {
                info2.setNation(stringBuffer.substring(16, 18));
            }
            info2.setBorn(stringBuffer.substring(18, 26));
            info2.setAddress(stringBuffer.substring(26, 61).trim());
            info2.setNo(stringBuffer.substring(61, 79).trim());
            info2.setApartment(stringBuffer.substring(79, 94).trim());
            info2.setPeriod(stringBuffer.substring(94, 110));
            if (!stringBuffer.substring(110, 119).equals("         ")) {
                info2.setPassNum(stringBuffer.substring(110, 119));
            }
            if (!stringBuffer.substring(119, 121).equals("  ")) {
                info2.setIssuesNum(stringBuffer.substring(119, 121));
            }
            if (!stringBuffer.substring(124, 125).equals(" ")) {
                info2.setCardSignal(stringBuffer.substring(124, 125));
            }
            info2.setHead_photo(Arrays.copyOfRange(dataByte, 256, dataByte.length));
        }
        StringBuilder builder = new StringBuilder();
        String temp = info2.getName().trim();
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
                info2.setName(builder.toString());
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
                info2.setName(builder.toString());
            }
        }
        try {
            info2.setNation(nation_list[Integer.parseInt(info2.getNation()) - 1]);
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
        }
        String temp2 = info2.getSex();
        if ("1".equals(temp2)) {
            info2.setSex("男 / M");
        } else if ("2".equals(temp2)) {
            info2.setSex("女 / F");
        }
        String temp3 = info2.getBorn().trim();
        if (temp3.length() >= 8) {
            info2.setBorn(formatDate(temp3));
        }
        String temp4 = info2.getPeriod().trim();
        if (temp4.length() >= 16) {
            info2.setPeriod(String.valueOf(formatDate(temp4.substring(0, 8))) + " - " + formatDate(temp4.substring(8)));
        }
        if (!"I".equals(info2.getCard_type())) {
            return info2;
        }
        info2.setApartment("公安部/Ministry of Public Security");
        return info2;
    }

    private void closeStream() {
        try {
            if (this.mReadThread != null) {
                this.mReadThread.interrupt();
            }
            if (this.mOutputStream != null) {
                this.mOutputStream.close();
            }
            if (this.mInputStream != null) {
                this.mInputStream.close();
            }
        } catch (IOException e) {
        }
        if (this.serial != null) {
            this.serial.close();
            this.serial = null;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

    private void checkId(String uart) {
        if (uart == null) {
            try {
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS550.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS550A.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS580A.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510A.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510A_NHW.ordinal()) {
                    if (this.serial == null) {
                        this.serial = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS0"), DEFAULT_BARTRATE, 0);
                    }
                } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS580.ordinal()) {
                    if (this.serial == null) {
                        this.serial = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS2"), DEFAULT_BARTRATE, 0);
                    }
                } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS550MTK.ordinal()) {
                    if (this.serial == null) {
                        this.serial = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyMT3"), DEFAULT_BARTRATE, 0);
                    }
                } else if ((SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS462.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS468.ordinal()) && this.serial == null) {
                    this.serial = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS3"), DEFAULT_BARTRATE, 0);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        } else if (this.serial == null) {
            this.serial = new SerialPort(true,"/system/xbin/su",new File(uart), DEFAULT_BARTRATE, 0);
        }
        if (this.mOutputStream == null) {
            this.mOutputStream = this.serial.getOutputStream();
        }
        if (this.mInputStream == null) {
            this.mInputStream = this.serial.getInputStream();
        }
        if (this.mReadThread == null) {
            this.mReadThread = new ReadThread();
            if (this.mReadThread.isInterrupted()) {
                this.mReadThread.start();
            }
        }
        byte[] bArr = new byte[10];
        bArr[0] = -86;
        bArr[1] = -86;
        bArr[2] = -86;
        bArr[3] = -106;
        bArr[4] = 105;
        bArr[6] = 3;
        bArr[7] = 32;
        bArr[8] = 1;
        bArr[9] = 34;
        sendCommand(bArr, 1);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }
        byte[] bArr2 = new byte[10];
        bArr2[0] = -86;
        bArr2[1] = -86;
        bArr2[2] = -86;
        bArr2[3] = -106;
        bArr2[4] = 105;
        bArr2[6] = 3;
        bArr2[7] = 32;
        bArr2[8] = 2;
        bArr2[9] = 33;
        sendCommand(bArr2, 2);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e4) {
            e4.printStackTrace();
        }
        byte[] bArr3 = new byte[10];
        bArr3[0] = -86;
        bArr3[1] = -86;
        bArr3[2] = -86;
        bArr3[3] = -106;
        bArr3[4] = 105;
        bArr3[6] = 3;
        bArr3[7] = 48;
        bArr3[8] = 16;
        bArr3[9] = 35;
        sendCommand(bArr3, 3);
    }

    private void sendCommand(byte[] cmdStr, int step2) {
        this.step = step2;
        if (step2 == 3) {
            this.idData = null;
        }
        int i = 0;
        while (i < cmdStr.length) {
            try {
                this.mOutputStream.write(cmdStr[i]);
                i++;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (SecurityException e2) {
                e2.printStackTrace();
                return;
            } catch (IOException e3) {
                e3.printStackTrace();
                return;
            }
        }
    }

    private class ReadThread extends Thread {
        public ReadThread() {
        }

        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    byte[] buffer = new byte[IdCard.READER_VID_BIG];
                    byte[] buffer2 = Arrays.copyOfRange(buffer, 0, IdCard.this.mInputStream.read(buffer));
                    if (IdCard.this.step == 3) {
                        IdCard.this.idData = ReaderUtils.merge(IdCard.this.idData, buffer2);
                    }
                    if (IdCard.this.idData != null) {
                        Log.d("idcard demo", "idData length:" + StringUtil.toHexString(IdCard.this.idData));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
