package com.telpo.tps550.api.printer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android_serialport_api.SerialPort;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.telpo.tps550.api.DeviceAlreadyOpenException;
import com.telpo.tps550.api.DeviceNotOpenException;
import com.telpo.tps550.api.ErrorCode;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.iccard.NotEnoughBufferException;
import com.telpo.tps550.api.util.ShellUtils;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

public class ThermalPrinter {
    public static final int ALGIN_LEFT = 0;
    public static final int ALGIN_MIDDLE = 1;
    public static final int ALGIN_RIGHT = 2;
    private static final int ARGB_MASK_BLUE = 255;
    private static final int ARGB_MASK_GREEN = 65280;
    private static final int ARGB_MASK_RED = 16711680;
    public static final int BARCODE_TYPE_CODABAR = 71;
    public static final int BARCODE_TYPE_CODE128 = 73;
    public static final int BARCODE_TYPE_CODE39 = 69;
    public static final int BARCODE_TYPE_CODE93 = 72;
    public static final int BARCODE_TYPE_EAN13 = 67;
    public static final int BARCODE_TYPE_EAN8 = 68;
    public static final int BARCODE_TYPE_ITF = 70;
    public static final int BARCODE_TYPE_UPCA = 65;
    public static final int BARCODE_TYPE_UPCE = 66;
    public static final int DIRECTION_BACK = 1;
    public static final int DIRECTION_FORWORD = 0;
    private static final String FILE_NAME = "/sdcard/tpsdk/printerVersion.txt";
    private static final int RGB565_MASK_BLUE = 31;
    private static final int RGB565_MASK_GREEN = 2016;
    private static final int RGB565_MASK_RED = 63488;
    public static final int STATUS_NO_PAPER = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OVER_FLOW = 3;
    public static final int STATUS_OVER_HEAT = 2;
    public static final int STATUS_UNKNOWN = 4;
    private static final String TAG = "ThermalPrinter";
    private static final String TPS550A_FILE_NAME = "/sdcard/tpsdk/tps550APrinterVersion.txt";
    public static final int WALK_DOTLINE = 0;
    public static final int WALK_LINE = 1;
    private static final int color = 128;
    private static UsbThermalPrinter mUsbThermalPrinter = null;
    private static boolean openFlag = false;
    private static int printerCheck = -1;
    private static int tps550aPrinterCheck = -1;
    public static boolean xon = false;

    protected static native int add_barcode(byte[] bArr, int i);

    protected static native int add_string(byte[] bArr, int i);

    protected static native int algin(int i);

    protected static native int check_status();

    protected static native int clear_string();

    protected static native int device_close();

    protected static native int device_open();

    protected static native int device_opened();

    protected static native int enlarge(int i, int i2);

    protected static native int get_printer_type();

    protected static native int get_version(byte[] bArr);

    protected static native int gray(int i);

    protected static native int highlight(boolean z);

    protected static native int indent(int i);

    protected static native int init();

    protected static native int line_space(int i);

    protected static native int paper_cut();

    protected static native int paper_cut_all();

    protected static native int print_and_walk(int i, int i2, int i3);

    protected static native int print_barcode(int i, byte[] bArr, int i2);

    protected static native int print_logo(int i, int i2, byte[] bArr);

    protected static native int search_mark(int i, int i2, int i3);

    protected static native int send_command(byte[] bArr, int i);

    protected static native int set_bold(int i);

    protected static native int set_font(int i);

    protected static native void sleep_ms(int i);

    protected static native int walk_paper(int i);

    static {
        if (Build.MODEL.equals("MTDP-618A") || Build.MODEL.equals("TPS650M")) {
            System.loadLibrary("telpo_printer_48");
        } else if (SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS650.ordinal()) {
            System.loadLibrary("telpo_printer");
        } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
            System.loadLibrary("telpo_printer");
        } else {
            System.loadLibrary("telpo_printer_581");
        }
    }

    protected static TelpoException getException(int ret) {
        switch (ret) {
            case ErrorCode.ERR_SYS_NO_INIT:
                return new DeviceNotOpenException();
            case ErrorCode.ERR_SYS_ALREADY_INIT:
                return new DeviceAlreadyOpenException();
            case ErrorCode.ERR_SYS_OVER_FLOW:
                return new NotEnoughBufferException();
            case ErrorCode.ERR_SYS_UNEXPECT:
                return new InternalErrorException();
            case ErrorCode.ERR_PRN_NO_PAPER:
                return new NoPaperException();
            case ErrorCode.ERR_PRN_OVER_TEMP:
                return new OverHeatException();
            case ErrorCode.ERR_PRN_GATE_OPEN:
                return new GateOpenException();
            case ErrorCode.ERR_PRN_NOT_CUT:
                return new PaperCutException();
            default:
                return new InternalErrorException();
        }
    }

    public ThermalPrinter(Context context) {
        if (!new File(TPS550A_FILE_NAME).exists() || getFileContent(TPS550A_FILE_NAME) == null) {
            Log.d("idcard demo", "tps550a printer choose");
            mUsbThermalPrinter = new UsbThermalPrinter(context);
            try {
                mUsbThermalPrinter.start(1);
            } catch (TelpoException e1) {
                e1.printStackTrace();
            }
            if (ShellUtils.execCommand("cat /sys/kernel/debug/usb/devices", false).successMsg.contains("USB Thermal Printer")) {
                tps550aPrinterCheck = 0;
            } else {
                tps550aPrinterCheck = 1;
            }
            writeData();
        } else if (getFileContent(TPS550A_FILE_NAME) != null && getFileContent(TPS550A_FILE_NAME).equals("USB_PRINTER")) {
            mUsbThermalPrinter = new UsbThermalPrinter(context);
        }
        Log.d("idcard demo", "check tps550a printer:" + getFileContent(TPS550A_FILE_NAME));
        if (getFileContent(TPS550A_FILE_NAME) != null && !getFileContent(TPS550A_FILE_NAME).equals("USB_PRINTER")) {
            mUsbThermalPrinter = null;
        }
    }

    private static void writeData() {
        Log.d("idcard demo", "printerCheck:" + printerCheck);
        Log.d("idcard demo", "tps550aPrinterCheck:" + tps550aPrinterCheck);
        if (printerCheck == 8) {
            writeTxtToFile("SY581", FILE_NAME);
        } else if (printerCheck == 7) {
            writeTxtToFile("PT72", FILE_NAME);
        } else if (tps550aPrinterCheck == 0) {
            writeTxtToFile("USB_PRINTER", TPS550A_FILE_NAME);
        } else if (tps550aPrinterCheck == 1) {
            writeTxtToFile("SERIAL_PRINTER", TPS550A_FILE_NAME);
        }
    }

    private static void writeTxtToFile(String strcontent, String fileName) {
        String strContent = strcontent;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + fileName);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Log.d("idcard demo", "file is exist");
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized java.lang.String getVersionNum(java.lang.String r7) {
        /*
            java.lang.Class<com.telpo.tps550.api.printer.ThermalPrinter> r4 = com.telpo.tps550.api.printer.ThermalPrinter.class
            monitor-enter(r4)
            r0 = 0
            java.lang.String r3 = "14"
            int r3 = r7.indexOf(r3)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            int r3 = r3 + 2
            java.lang.String r5 = "14"
            int r5 = r7.indexOf(r5)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            int r5 = r5 + 4
            java.lang.String r2 = r7.substring(r3, r5)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r3 = "idcard demo"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r6 = "realVersion:"
            r5.<init>(r6)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.StringBuilder r5 = r5.append(r2)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            android.util.Log.d(r3, r5)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            int r3 = java.lang.Integer.parseInt(r2)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r5 = "91"
            int r5 = java.lang.Integer.parseInt(r5)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            int r1 = r3 - r5
            java.lang.String r3 = "idcard demo"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r6 = "nowVersion:"
            r5.<init>(r6)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.StringBuilder r5 = r5.append(r1)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r5 = r5.toString()     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            android.util.Log.d(r3, r5)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r5 = "1."
            r3.<init>(r5)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            int r5 = r1 + 45
            java.lang.StringBuilder r3 = r3.append(r5)     // Catch:{ Exception -> 0x0062, all -> 0x005f }
            java.lang.String r0 = r3.toString()     // Catch:{ Exception -> 0x0062, all -> 0x005f }
        L_0x005d:
            monitor-exit(r4)
            return r0
        L_0x005f:
            r3 = move-exception
            monitor-exit(r4)
            throw r3
        L_0x0062:
            r3 = move-exception
            goto L_0x005d
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.ThermalPrinter.getVersionNum(java.lang.String):java.lang.String");
    }

    public static synchronized void start() throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.start(1);
            } else if (openFlag) {
                throw new DeviceAlreadyOpenException();
            } else {
                int ret = device_open();
                if (ret == 0) {
                    openFlag = true;
                } else {
                    throw getException(ret);
                }
            }
        }
    }

    public static synchronized void start(Context context) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.start(1);
            } else if (openFlag) {
                throw new DeviceAlreadyOpenException();
            } else {
                int ret = device_open();
                if (ret == 0) {
                    openFlag = true;
                    context.sendBroadcast(new Intent("com.telpo.printer.thermalprinter.start"));
                } else {
                    throw getException(ret);
                }
            }
        }
    }

    public static synchronized void start(Context context, int i) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            Log.d("idcard demo", "device open");
            int device_opened = device_opened();
        }
    }

    public static synchronized void reset() throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.reset();
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                int ret = init();
                if (ret != 0) {
                    throw getException(ret);
                }
            } else {
                ThermalPrinterSY581.reset();
            }
        }
    }

    public static synchronized void walkPaper(int line) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.walkPaper(line);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (line <= 0) {
                throw new IllegalArgumentException();
            } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS681.ordinal()) {
                walkPaper681(line);
            } else {
                int ret = walk_paper(line);
                if (ret != 0) {
                    throw getException(ret);
                }
            }
        }
    }

    public static synchronized void walkPaper681(int line) {
        int walkLine;
        synchronized (ThermalPrinter.class) {
            try {
                StringBuffer sBuffer = new StringBuffer();
                if (line >= 10) {
                    walkLine = line / 10;
                } else {
                    walkLine = line % 10;
                }
                for (int i = 0; i < walkLine; i++) {
                    sBuffer.append(" \n");
                }
                reset();
                addString(sBuffer.toString());
                printString();
            } catch (TelpoException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public static synchronized void stop() {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.stop();
            } else if (openFlag) {
                device_close();
                openFlag = false;
            }
        }
    }

    public static synchronized void stop(Context context) {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.stop();
            } else if (openFlag) {
                device_close();
                openFlag = false;
                context.sendBroadcast(new Intent("com.telpo.printer.thermalprinter.stop"));
            }
        }
    }

    public static synchronized void stop(Context context, int i) {
        synchronized (ThermalPrinter.class) {
            device_close();
        }
    }

    public static synchronized int checkStatus() throws TelpoException {
        int i;
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter == null) {
                if (openFlag) {
                    int ret = 0;
                    if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                        ret = check_status();
                    } else {
                        ThermalPrinterSY581.checkStatus();
                    }
                    switch (ret) {
                        case 0:
                            i = 0;
                            break;
                        case ErrorCode.ERR_SYS_OVER_FLOW:
                            i = 3;
                            break;
                        case ErrorCode.ERR_PRN_NO_PAPER:
                            i = 1;
                            break;
                        case ErrorCode.ERR_PRN_OVER_TEMP:
                            i = 2;
                            break;
                        default:
                            i = 4;
                            break;
                    }
                } else {
                    throw new DeviceNotOpenException();
                }
            } else {
                i = mUsbThermalPrinter.checkStatus();
            }
        }
        return i;
    }

    public static synchronized void enlargeFontSize(int widthMultiple, int heightMultiple) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.enlargeFontSize(widthMultiple, heightMultiple);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else {
                int ret = enlarge(widthMultiple, heightMultiple);
                if (ret != 0) {
                    throw getException(ret);
                }
            }
        }
    }

    public static synchronized void setFontSize(int type) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.setFontSize(type);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                int ret = set_font(type);
                if (ret != 0) {
                    throw getException(ret);
                }
            } else {
                ThermalPrinterSY581.setFont(type);
            }
        }
    }

    public static synchronized void setTem(int tem) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (getFileContent(FILE_NAME) != null && getFileContent(FILE_NAME).equals("SY581")) {
                ThermalPrinterSY581.setTem(tem);
            }
        }
    }

    public static synchronized void setHighlight(boolean mode) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.setHighlight(mode);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else {
                int ret = highlight(mode);
                if (ret != 0) {
                    throw getException(ret);
                }
            }
        }
    }

    public static synchronized void setGray(int level) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.setGray(level);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                int ret = gray(level);
                if (ret != 0) {
                    throw getException(ret);
                }
            } else {
                ThermalPrinterSY581.setGray(level);
            }
        }
    }

    public static synchronized void setAlgin(int mode) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.setAlgin(mode);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                int ret = algin(mode);
                if (ret != 0) {
                    throw getException(ret);
                }
            } else {
                ThermalPrinterSY581.setAlign(mode);
            }
        }
    }

    public static synchronized void addString(String content) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.addString(content);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else {
                if (content != null) {
                    if (content.length() != 0) {
                        if (getFileContent(FILE_NAME) != null && getFileContent(FILE_NAME).equals("SY581")) {
                            content = " " + content;
                        }
                        try {
                            byte[] text = content.getBytes("GBK");
                            int ret = add_string(text, text.length);
                            if (ret != 0) {
                                throw getException(ret);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            throw new InternalErrorException();
                        }
                    }
                }
                throw new NullPointerException();
            }
        }
    }

    public static synchronized void addBarcode(String barcode) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (!openFlag) {
                throw new DeviceNotOpenException();
            }
            if (barcode != null) {
                if (barcode.length() != 0) {
                    byte[] barcodeCmd = new byte[53];
                    barcodeCmd[0] = 29;
                    barcodeCmd[1] = 104;
                    barcodeCmd[2] = 84;
                    barcodeCmd[3] = 29;
                    barcodeCmd[4] = 108;
                    Bitmap bitmap = CreateCode(barcode, BarcodeFormat.CODE_128, 360, 108);
                    int ss = 5;
                    int widthOctet = bitmap.getWidth() / 8;
                    int width = 0;
                    int column = 0;
                    while (width < widthOctet) {
                        int temp = 0;
                        for (int i = 0; i < 8; i++) {
                            int pix = bitmap.getPixel(i + column, 0);
                            if (((ARGB_MASK_RED & pix) >> 16) <= color || ((ARGB_MASK_GREEN & pix) >> 8) <= color || (pix & ARGB_MASK_BLUE) <= color) {
                                temp = (temp << 1) + 1;
                            } else {
                                temp <<= 1;
                            }
                        }
                        barcodeCmd[ss] = (byte) temp;
                        ss++;
                        width++;
                        column += 8;
                    }
                    int ret = add_barcode(barcodeCmd, barcodeCmd.length);
                    if (ret != 0) {
                        throw getException(ret);
                    }
                }
            }
            throw new NullPointerException();
        }
    }

    public static synchronized void clearString() throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.clearString();
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else {
                int ret = clear_string();
                if (ret != 0) {
                    throw getException(ret);
                }
            }
        }
    }

    public static synchronized void printString() throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.printString();
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else {
                int ret = print_and_walk(0, 0, 0);
                if (ret != 0) {
                    throw getException(ret);
                }
            }
        }
    }

    public static synchronized void printStringAndWalk(int direction, int mode, int lines) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.printStringAndWalk(direction, mode, lines);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (direction != 1 && direction != 0) {
                throw new IllegalArgumentException();
            } else if (mode == 1 || mode == 0) {
                int ret = print_and_walk(direction, mode, lines);
                if (ret != 0) {
                    throw getException(ret);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public static synchronized void setLineSpace(int lineSpace) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.setLineSpace(lineSpace);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (lineSpace < 0 || lineSpace > ARGB_MASK_BLUE) {
                throw new IllegalArgumentException();
            } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                int ret = line_space(lineSpace);
                if (ret != 0) {
                    throw getException(ret);
                }
            } else {
                ThermalPrinterSY581.setLineSpace(lineSpace);
            }
        }
    }

    public static synchronized void setLeftIndent(int space) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.setLeftIndent(space);
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else if (space < 0 || space > ARGB_MASK_BLUE) {
                throw new IllegalArgumentException();
            } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                int ret = indent(space);
                if (ret != 0) {
                    throw getException(ret);
                }
            } else {
                ThermalPrinterSY581.setLeftDistance(space);
            }
        }
    }

    private static synchronized void printLogo581SetLeft(int left, Bitmap bitmap) {
        Bitmap picture;
        synchronized (ThermalPrinter.class) {
            SerialPort serial = null;
            byte[] cmdStr = null;
            int count = 0;
            try {
                serial = SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS650T.ordinal() ? new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS0"), 460800, 0) : new SerialPort(new File("/dev/ttyS4"), 460800, 0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (SecurityException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            if (bitmap.getWidth() % 8 != 0) {
                picture = zoomImg(bitmap, ((bitmap.getWidth() / 8) + 1) * 8, bitmap.getHeight());
            } else {
                picture = bitmap;
            }
            try {
                byte[] picture_haschange = changeBitmap(picture);
                cmdStr = new byte[(picture_haschange.length + 8)];
                cmdStr[0] = 29;
                cmdStr[1] = 118;
                cmdStr[2] = StringUtil.toBytes(Integer.toHexString(left))[0];
                String xl = Integer.toHexString((picture.getWidth() / 8) % 256);
                String xh = Integer.toHexString((picture.getWidth() / 8) / 256);
                String yl = Integer.toHexString(picture.getHeight() % 256);
                String yh = Integer.toHexString(picture.getHeight() / 256);
                if (xl.length() == 1) {
                    xl = "0" + xl;
                }
                if (xh.length() == 1) {
                    xh = "0" + xh;
                }
                if (yl.length() == 1) {
                    yl = "0" + yl;
                }
                if (yh.length() == 1) {
                    yh = "0" + yh;
                }
                cmdStr[3] = parseHexStr2Byte(xl)[0];
                cmdStr[4] = parseHexStr2Byte(xh)[0];
                cmdStr[5] = parseHexStr2Byte(yl)[0];
                cmdStr[6] = parseHexStr2Byte(yh)[0];
                int i = 8;
                int j = 0;
                while (i < cmdStr.length) {
                    cmdStr[i] = picture_haschange[j];
                    i++;
                    j++;
                }
            } catch (TelpoException e22) {
                e22.printStackTrace();
            }
            OutputStream mOutputStream = serial.getOutputStream();
            xon = true;
            while (cmdStr.length - count > 150) {
                try {
                    if (xon) {
                        mOutputStream.write(cmdStr, count, 150);
                        count += 150;
                        try {
                            Thread.sleep(4);
                        } catch (InterruptedException e4) {
                            e4.printStackTrace();
                        }
                    }
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            try {
                mOutputStream.write(cmdStr, count, cmdStr.length - count);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (serial != null) {
                serial.close();
            }
            try {
                mOutputStream.close();
            } catch (IOException e6) {
            }
        }
        return;
    }

    private static synchronized void printLogo581(Bitmap bitmap) {
        Bitmap picture;
        synchronized (ThermalPrinter.class) {
            SerialPort serial = null;
            byte[] cmdStr = null;
            int count = 0;
            try {
                serial = SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS650T.ordinal() ? new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS0"), 460800, 0) : new SerialPort(new File("/dev/ttyS4"), 460800, 0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (SecurityException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            if (bitmap.getWidth() % 8 != 0) {
                picture = zoomImg(bitmap, ((bitmap.getWidth() / 8) + 1) * 8, bitmap.getHeight());
            } else {
                picture = bitmap;
            }
            try {
                byte[] picture_haschange = changeBitmap(picture);
                cmdStr = new byte[(picture_haschange.length + 8)];
                cmdStr[0] = 29;
                cmdStr[1] = 118;
                cmdStr[2] = 48;
                cmdStr[3] = 0;
                String xl = Integer.toHexString((picture.getWidth() / 8) % 256);
                String xh = Integer.toHexString((picture.getWidth() / 8) / 256);
                String yl = Integer.toHexString(picture.getHeight() % 256);
                String yh = Integer.toHexString(picture.getHeight() / 256);
                if (xl.length() == 1) {
                    xl = "0" + xl;
                }
                if (xh.length() == 1) {
                    xh = "0" + xh;
                }
                if (yl.length() == 1) {
                    yl = "0" + yl;
                }
                if (yh.length() == 1) {
                    yh = "0" + yh;
                }
                cmdStr[4] = parseHexStr2Byte(xl)[0];
                cmdStr[5] = parseHexStr2Byte(xh)[0];
                cmdStr[6] = parseHexStr2Byte(yl)[0];
                cmdStr[7] = parseHexStr2Byte(yh)[0];
                int i = 8;
                int j = 0;
                while (i < cmdStr.length) {
                    cmdStr[i] = picture_haschange[j];
                    i++;
                    j++;
                }
            } catch (TelpoException e22) {
                e22.printStackTrace();
            }
            OutputStream mOutputStream = serial.getOutputStream();
            xon = true;
            while (cmdStr.length - count > 150) {
                try {
                    if (xon) {
                        mOutputStream.write(cmdStr, count, 150);
                        count += 150;
                        try {
                            Thread.sleep(4);
                        } catch (InterruptedException e4) {
                            e4.printStackTrace();
                        }
                    }
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            try {
                mOutputStream.write(cmdStr, count, cmdStr.length - count);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (serial != null) {
                serial.close();
            }
            try {
                mOutputStream.close();
            } catch (IOException e6) {
            }
        }
        return;
    }

    private static synchronized byte[] parseHexStr2Byte(String hexStr) {
        byte[] result;
        synchronized (ThermalPrinter.class) {
            if (hexStr.length() < 1) {
                result = null;
            } else {
                result = new byte[(hexStr.length() / 2)];
                for (int i = 0; i < hexStr.length() / 2; i++) {
                    int high = Integer.parseInt(hexStr.substring(i * 2, (i * 2) + 1), 16);
                    result[i] = (byte) ((high * 16) + Integer.parseInt(hexStr.substring((i * 2) + 1, (i * 2) + 2), 16));
                }
            }
        }
        return result;
    }

    private static synchronized Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        Bitmap newbm;
        synchronized (ThermalPrinter.class) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            Matrix matrix = new Matrix();
            matrix.postScale(((float) newWidth) / ((float) width), ((float) newHeight) / ((float) height));
            newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        }
        return newbm;
    }

    private static synchronized byte[] changeBitmap(Bitmap image) throws TelpoException {
        byte[] Imagelogo;
        int printWidth;
        int printHeight;
        synchronized (ThermalPrinter.class) {
            int ss = 0;
            if (image == null) {
                throw new NullPointerException();
            }
            int printer_type = -1;
            if (getFileContent(FILE_NAME) != null && getFileContent(FILE_NAME).equals("SY581")) {
                printer_type = 8;
            }
            if (printer_type == 5 || printer_type == 3 || printer_type == 4 || printer_type == 8) {
                if (image.getWidth() > 576 || image.getHeight() < 1) {
                    throw new IllegalArgumentException("The width or the height of the image to print is illegal!");
                }
                int widthLeft = image.getWidth() % 8;
                if (widthLeft != 0) {
                    printWidth = (image.getWidth() - widthLeft) + 8;
                } else {
                    printWidth = image.getWidth();
                }
                byte[] Imagelogo2 = new byte[((printWidth / 8) * image.getHeight())];
                if (image.getConfig().equals(Bitmap.Config.ARGB_8888)) {
                    int widthOctet = image.getWidth() / 8;
                    for (int row = 0; row < image.getHeight(); row++) {
                        int width = 0;
                        int column = 0;
                        while (width < widthOctet) {
                            int temp = 0;
                            for (int i = 0; i < 8; i++) {
                                int pix = image.getPixel(i + column, row);
                                if (((ARGB_MASK_RED & pix) >> 16) <= color || ((ARGB_MASK_GREEN & pix) >> 8) <= color || (pix & ARGB_MASK_BLUE) <= color) {
                                    temp = (temp << 1) + 1;
                                } else {
                                    temp <<= 1;
                                }
                            }
                            Imagelogo2[ss] = (byte) temp;
                            ss++;
                            width++;
                            column += 8;
                        }
                        if (widthLeft != 0) {
                            int temp2 = 0;
                            for (int i2 = 0; i2 < widthLeft; i2++) {
                                int pix2 = image.getPixel(i2 + column, row);
                                if (((ARGB_MASK_RED & pix2) >> 16) <= color || ((ARGB_MASK_GREEN & pix2) >> 8) <= color || (pix2 & ARGB_MASK_BLUE) <= color) {
                                    temp2 = (temp2 << 1) + 1;
                                } else {
                                    temp2 <<= 1;
                                }
                            }
                            Imagelogo2[ss] = (byte) (temp2 << (8 - widthLeft));
                            ss++;
                        }
                    }
                    byte[] bArr = Imagelogo2;
                    Imagelogo = Imagelogo2;
                } else {
                    if (image.getConfig().equals(Bitmap.Config.ALPHA_8)) {
                        int widthOctet2 = image.getWidth() / 8;
                        for (int row2 = 0; row2 < image.getHeight(); row2++) {
                            int width2 = 0;
                            int column2 = 0;
                            while (width2 < widthOctet2) {
                                int temp3 = 0;
                                for (int i3 = 0; i3 < 8; i3++) {
                                    if ((image.getPixel(i3 + column2, row2) & ARGB_MASK_BLUE) > color) {
                                        temp3 <<= 1;
                                    } else {
                                        temp3 = (temp3 << 1) + 1;
                                    }
                                }
                                Imagelogo2[ss] = (byte) temp3;
                                ss++;
                                width2++;
                                column2 += 8;
                            }
                            if (widthLeft != 0) {
                                int temp4 = 0;
                                for (int i4 = 0; i4 < widthLeft; i4++) {
                                    if ((image.getPixel(i4 + column2, row2) & ARGB_MASK_BLUE) > color) {
                                        temp4 <<= 1;
                                    } else {
                                        temp4 = (temp4 << 1) + 1;
                                    }
                                }
                                Imagelogo2[ss] = (byte) (temp4 << (8 - widthLeft));
                                ss++;
                            }
                        }
                        byte[] bArr2 = Imagelogo2;
                        Imagelogo = Imagelogo2;
                    } else {
                        if (image.getConfig().equals(Bitmap.Config.RGB_565)) {
                            int widthOctet3 = image.getWidth() / 8;
                            for (int row3 = 0; row3 < image.getHeight(); row3++) {
                                int width3 = 0;
                                int column3 = 0;
                                while (width3 < widthOctet3) {
                                    int temp5 = 0;
                                    for (int i5 = 0; i5 < 8; i5++) {
                                        int pix3 = image.getPixel(i5 + column3, row3);
                                        if (((RGB565_MASK_RED & pix3) >> 11) <= 15 || ((pix3 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix3 & RGB565_MASK_BLUE) <= 15) {
                                            temp5 = (temp5 << 1) + 1;
                                        } else {
                                            temp5 <<= 1;
                                        }
                                    }
                                    Imagelogo2[ss] = (byte) temp5;
                                    ss++;
                                    width3++;
                                    column3 += 8;
                                }
                                if (widthLeft != 0) {
                                    int temp6 = 0;
                                    for (int i6 = 0; i6 < widthLeft; i6++) {
                                        int pix4 = image.getPixel(i6 + column3, row3);
                                        if (((RGB565_MASK_RED & pix4) >> 11) <= 15 || ((pix4 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix4 & RGB565_MASK_BLUE) <= 15) {
                                            temp6 = (temp6 << 1) + 1;
                                        } else {
                                            temp6 <<= 1;
                                        }
                                    }
                                    Imagelogo2[ss] = (byte) (temp6 << (8 - widthLeft));
                                    ss++;
                                }
                            }
                            byte[] bArr3 = Imagelogo2;
                            Imagelogo = Imagelogo2;
                        } else {
                            byte[] bArr4 = Imagelogo2;
                            Imagelogo = Imagelogo2;
                        }
                    }
                }
            } else if (image.getWidth() > 384 || image.getHeight() < 1) {
                throw new IllegalArgumentException("The width or the height of the image to print is illegal!");
            } else {
                if (image.getHeight() % 8 != 0) {
                    printHeight = ((image.getHeight() / 8) + 1) * 8;
                } else {
                    printHeight = image.getHeight();
                }
                byte[] Imagelogo3 = new byte[((image.getWidth() * printHeight) / 8)];
                if (image.getConfig().equals(Bitmap.Config.ARGB_8888)) {
                    for (int row4 = 0; row4 < image.getHeight() / 8; row4++) {
                        for (int with = 0; with < image.getWidth(); with++) {
                            int temp7 = 0;
                            for (int height = row4 * 8; height < (row4 * 8) + 8; height++) {
                                int pix5 = image.getPixel(with, height);
                                if (((ARGB_MASK_RED & pix5) >> 16) <= color || ((ARGB_MASK_GREEN & pix5) >> 8) <= color || (pix5 & ARGB_MASK_BLUE) <= color) {
                                    temp7 = (temp7 << 1) + 1;
                                } else {
                                    temp7 <<= 1;
                                }
                            }
                            Imagelogo3[ss] = (byte) temp7;
                            ss++;
                        }
                    }
                } else {
                    if (image.getConfig().equals(Bitmap.Config.ALPHA_8)) {
                        for (int row5 = 0; row5 < image.getHeight() / 8; row5++) {
                            for (int with2 = 0; with2 < image.getWidth(); with2++) {
                                int temp8 = 0;
                                for (int height2 = row5 * 8; height2 < (row5 * 8) + 8; height2++) {
                                    if ((image.getPixel(with2, height2) & ARGB_MASK_BLUE) > color) {
                                        temp8 = (temp8 * 2) << 1;
                                    } else {
                                        temp8 = (temp8 << 1) + 1;
                                    }
                                }
                                Imagelogo3[ss] = (byte) temp8;
                                ss++;
                            }
                        }
                    } else {
                        if (image.getConfig().equals(Bitmap.Config.RGB_565)) {
                            for (int row6 = 0; row6 < image.getHeight() / 8; row6++) {
                                for (int with3 = 0; with3 < image.getWidth(); with3++) {
                                    int temp9 = 0;
                                    for (int height3 = row6 * 8; height3 < (row6 * 8) + 8; height3++) {
                                        int pix6 = image.getPixel(with3, height3);
                                        if (((RGB565_MASK_RED & pix6) >> 11) <= 15 || ((pix6 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix6 & RGB565_MASK_BLUE) <= 15) {
                                            temp9 = (temp9 << 1) + 1;
                                        } else {
                                            temp9 <<= 1;
                                        }
                                    }
                                    Imagelogo3[ss] = (byte) temp9;
                                    ss++;
                                }
                            }
                        }
                    }
                }
                byte[] bArr5 = Imagelogo3;
                Imagelogo = Imagelogo3;
            }
        }
        return Imagelogo;
    }

    public static synchronized void printLogo(int width, int height, char[] logo) throws TelpoException {
        synchronized (ThermalPrinter.class) {
            if (!openFlag) {
                throw new DeviceNotOpenException("The printer has not been init!");
            }
            int printer_type = get_printer_type();
            if (printer_type == 3 || printer_type == 4 || printer_type == 5) {
                if (width > 576 || width % 8 != 0) {
                    throw new IllegalArgumentException("The width of the image to print is illegal!");
                }
            } else if (width > 384 || height % 8 != 0) {
                throw new IllegalArgumentException("The width or the height of the image to print is illegal!");
            }
            byte[] logoBytes = new byte[logo.length];
            for (int i = 0; i < logo.length; i++) {
                logoBytes[i] = (byte) logo[i];
            }
            int ret = print_logo(width, height, logoBytes);
            if (ret != 0) {
                throw getException(ret);
            }
        }
    }

    public static synchronized void printLogo581Left(int left, Bitmap image) {
        synchronized (ThermalPrinter.class) {
            printLogo581SetLeft(left, image);
        }
    }

    public static synchronized void printLogo(Bitmap image) throws TelpoException {
        int ret;
        int printWidth;
        int printHeight;
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                mUsbThermalPrinter.printLogo(image, false);
            } else if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                int ss = 0;
                if (!openFlag) {
                    throw new DeviceNotOpenException("The printer has not been init!");
                } else if (image == null) {
                    throw new NullPointerException();
                } else {
                    int printer_type = SystemUtil.getPrinterType();
                    if (Build.MODEL.equals("MTDP-618A") || Build.MODEL.equals("TPS650M")) {
                        printer_type = 2;
                    }
                    if (printer_type == 6 || printer_type == 7) {
                        int img_height = image.getHeight();
                        int img_width = image.getWidth();
                        int image_row_bytes = (img_height + 7) >> 3;
                        byte[] bitmap_mode_cmd = {27, 42, 1, (byte) (img_width & ARGB_MASK_BLUE), (byte) ((img_width >> 8) & ARGB_MASK_BLUE)};
                        byte[] buffer = new byte[((img_width * image_row_bytes) + image_row_bytes + (bitmap_mode_cmd.length * image_row_bytes) + 6 + 3 + 2)];
                        buffer[0] = 29;
                        buffer[1] = 69;
                        buffer[2] = 14;
                        buffer[3] = 27;
                        buffer[4] = 51;
                        buffer[5] = 0;
                        int row_index = 0;
                        int h = 0;
                        int ss2 = 6;
                        while (row_index < image_row_bytes) {
                            System.arraycopy(bitmap_mode_cmd, 0, buffer, ss2, bitmap_mode_cmd.length);
                            int col_index = 0;
                            int ss3 = ss2 + bitmap_mode_cmd.length;
                            while (col_index < img_width) {
                                int temp = 0;
                                int i = 0;
                                while (i < 8 && i + h < img_height) {
                                    int Pixel_val = image.getPixel(col_index, i + h);
                                    if (((ARGB_MASK_RED & Pixel_val) >> 16) <= color || ((ARGB_MASK_GREEN & Pixel_val) >> 8) <= color || (Pixel_val & ARGB_MASK_BLUE) <= color) {
                                        temp = (temp << 1) + 1;
                                    } else {
                                        temp <<= 1;
                                    }
                                    i++;
                                }
                                buffer[ss3] = (byte) temp;
                                col_index++;
                                ss3++;
                            }
                            buffer[ss3] = 10;
                            row_index++;
                            h += 8;
                            ss2 = ss3 + 1;
                        }
                        int ss4 = ss2 + 1;
                        buffer[ss2] = 29;
                        int ss5 = ss4 + 1;
                        buffer[ss4] = 69;
                        int ss6 = ss5 + 1;
                        buffer[ss5] = 1;
                        int ss7 = ss6 + 1;
                        buffer[ss6] = 27;
                        int i2 = ss7 + 1;
                        buffer[ss7] = 64;
                        ret = print_logo(buffer.length, 0, buffer);
                    } else if (printer_type == 5 || printer_type == 3 || printer_type == 4) {
                        if (image.getWidth() > 576 || image.getHeight() < 1) {
                            throw new IllegalArgumentException("The width or the height of the image to print is illegal!");
                        }
                        int widthLeft = image.getWidth() % 8;
                        if (widthLeft != 0) {
                            printWidth = (image.getWidth() - widthLeft) + 8;
                        } else {
                            printWidth = image.getWidth();
                        }
                        byte[] Imagelogo = new byte[((printWidth / 8) * image.getHeight())];
                        if (image.getConfig().equals(Bitmap.Config.ARGB_8888)) {
                            int widthOctet = image.getWidth() / 8;
                            for (int row = 0; row < image.getHeight(); row++) {
                                int width = 0;
                                int column = 0;
                                while (width < widthOctet) {
                                    int temp2 = 0;
                                    for (int i3 = 0; i3 < 8; i3++) {
                                        int pix = image.getPixel(i3 + column, row);
                                        if (((ARGB_MASK_RED & pix) >> 16) <= color || ((ARGB_MASK_GREEN & pix) >> 8) <= color || (pix & ARGB_MASK_BLUE) <= color) {
                                            temp2 = (temp2 << 1) + 1;
                                        } else {
                                            temp2 <<= 1;
                                        }
                                    }
                                    Imagelogo[ss] = (byte) temp2;
                                    ss++;
                                    width++;
                                    column += 8;
                                }
                                if (widthLeft != 0) {
                                    int temp3 = 0;
                                    for (int i4 = 0; i4 < widthLeft; i4++) {
                                        int pix2 = image.getPixel(i4 + column, row);
                                        if (((ARGB_MASK_RED & pix2) >> 16) <= color || ((ARGB_MASK_GREEN & pix2) >> 8) <= color || (pix2 & ARGB_MASK_BLUE) <= color) {
                                            temp3 = (temp3 << 1) + 1;
                                        } else {
                                            temp3 <<= 1;
                                        }
                                    }
                                    Imagelogo[ss] = (byte) (temp3 << (8 - widthLeft));
                                    ss++;
                                }
                            }
                            Log.i(TAG, "dealing ARGB_8888 image");
                        } else {
                            if (image.getConfig().equals(Bitmap.Config.ALPHA_8)) {
                                int widthOctet2 = image.getWidth() / 8;
                                for (int row2 = 0; row2 < image.getHeight(); row2++) {
                                    int width2 = 0;
                                    int column2 = 0;
                                    while (width2 < widthOctet2) {
                                        int temp4 = 0;
                                        for (int i5 = 0; i5 < 8; i5++) {
                                            if ((image.getPixel(i5 + column2, row2) & ARGB_MASK_BLUE) > color) {
                                                temp4 <<= 1;
                                            } else {
                                                temp4 = (temp4 << 1) + 1;
                                            }
                                        }
                                        Imagelogo[ss] = (byte) temp4;
                                        ss++;
                                        width2++;
                                        column2 += 8;
                                    }
                                    if (widthLeft != 0) {
                                        int temp5 = 0;
                                        for (int i6 = 0; i6 < widthLeft; i6++) {
                                            if ((image.getPixel(i6 + column2, row2) & ARGB_MASK_BLUE) > color) {
                                                temp5 <<= 1;
                                            } else {
                                                temp5 = (temp5 << 1) + 1;
                                            }
                                        }
                                        Imagelogo[ss] = (byte) (temp5 << (8 - widthLeft));
                                        ss++;
                                    }
                                }
                                Log.i(TAG, "dealing ALPHA_8 image");
                            } else {
                                if (image.getConfig().equals(Bitmap.Config.RGB_565)) {
                                    int widthOctet3 = image.getWidth() / 8;
                                    for (int row3 = 0; row3 < image.getHeight(); row3++) {
                                        int width3 = 0;
                                        int column3 = 0;
                                        while (width3 < widthOctet3) {
                                            int temp6 = 0;
                                            for (int i7 = 0; i7 < 8; i7++) {
                                                int pix3 = image.getPixel(i7 + column3, row3);
                                                if (((RGB565_MASK_RED & pix3) >> 11) <= 15 || ((pix3 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix3 & RGB565_MASK_BLUE) <= 15) {
                                                    temp6 = (temp6 << 1) + 1;
                                                } else {
                                                    temp6 <<= 1;
                                                }
                                            }
                                            Imagelogo[ss] = (byte) temp6;
                                            ss++;
                                            width3++;
                                            column3 += 8;
                                        }
                                        if (widthLeft != 0) {
                                            int temp7 = 0;
                                            for (int i8 = 0; i8 < widthLeft; i8++) {
                                                int pix4 = image.getPixel(i8 + column3, row3);
                                                if (((RGB565_MASK_RED & pix4) >> 11) <= 15 || ((pix4 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix4 & RGB565_MASK_BLUE) <= 15) {
                                                    temp7 = (temp7 << 1) + 1;
                                                } else {
                                                    temp7 <<= 1;
                                                }
                                            }
                                            Imagelogo[ss] = (byte) (temp7 << (8 - widthLeft));
                                            ss++;
                                        }
                                    }
                                    Log.i(TAG, "dealing RGB_565 image");
                                } else {
                                    Log.e(TAG, "unsupport image formate!");
                                }
                            }
                        }
                        ret = print_logo(printWidth, image.getHeight(), Imagelogo);
                    } else if (image.getWidth() > 384 || image.getHeight() < 1) {
                        throw new IllegalArgumentException("The width or the height of the image to print is illegal!");
                    } else {
                        if (image.getHeight() % 8 != 0) {
                            printHeight = ((image.getHeight() / 8) + 1) * 8;
                        } else {
                            printHeight = image.getHeight();
                        }
                        byte[] Imagelogo2 = new byte[((image.getWidth() * printHeight) / 8)];
                        if (image.getConfig().equals(Bitmap.Config.ARGB_8888)) {
                            for (int row4 = 0; row4 < image.getHeight() / 8; row4++) {
                                for (int with = 0; with < image.getWidth(); with++) {
                                    int temp8 = 0;
                                    for (int height = row4 * 8; height < (row4 * 8) + 8; height++) {
                                        int pix5 = image.getPixel(with, height);
                                        if (((ARGB_MASK_RED & pix5) >> 16) <= color || ((ARGB_MASK_GREEN & pix5) >> 8) <= color || (pix5 & ARGB_MASK_BLUE) <= color) {
                                            temp8 = (temp8 << 1) + 1;
                                        } else {
                                            temp8 <<= 1;
                                        }
                                    }
                                    Imagelogo2[ss] = (byte) temp8;
                                    ss++;
                                }
                            }
                            Log.i(TAG, "dealing ARGB_8888 image");
                        } else {
                            if (image.getConfig().equals(Bitmap.Config.ALPHA_8)) {
                                for (int row5 = 0; row5 < image.getHeight() / 8; row5++) {
                                    for (int with2 = 0; with2 < image.getWidth(); with2++) {
                                        int temp9 = 0;
                                        for (int height2 = row5 * 8; height2 < (row5 * 8) + 8; height2++) {
                                            if ((image.getPixel(with2, height2) & ARGB_MASK_BLUE) > color) {
                                                temp9 = (temp9 * 2) << 1;
                                            } else {
                                                temp9 = (temp9 << 1) + 1;
                                            }
                                        }
                                        Imagelogo2[ss] = (byte) temp9;
                                        ss++;
                                    }
                                }
                                Log.i(TAG, "dealing ALPHA_8 image");
                            } else {
                                if (image.getConfig().equals(Bitmap.Config.RGB_565)) {
                                    for (int row6 = 0; row6 < image.getHeight() / 8; row6++) {
                                        for (int with3 = 0; with3 < image.getWidth(); with3++) {
                                            int temp10 = 0;
                                            for (int height3 = row6 * 8; height3 < (row6 * 8) + 8; height3++) {
                                                int pix6 = image.getPixel(with3, height3);
                                                if (((RGB565_MASK_RED & pix6) >> 11) <= 15 || ((pix6 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix6 & RGB565_MASK_BLUE) <= 15) {
                                                    temp10 = (temp10 << 1) + 1;
                                                } else {
                                                    temp10 <<= 1;
                                                }
                                            }
                                            Imagelogo2[ss] = (byte) temp10;
                                            ss++;
                                        }
                                    }
                                    Log.i(TAG, "dealing RGB_565 image");
                                } else {
                                    Log.e(TAG, "unsupport image formate!");
                                }
                            }
                        }
                        ret = print_logo(image.getWidth(), printHeight, Imagelogo2);
                    }
                    if (ret != 0) {
                        if (ret == 61441) {
                            throw new IllegalArgumentException();
                        }
                        throw getException(ret);
                    }
                }
            } else {
                printLogo581(image);
            }
        }
    }

    public static synchronized String getVersion() throws TelpoException {
        String ver;
        synchronized (ThermalPrinter.class) {
            if (mUsbThermalPrinter != null) {
                ver = mUsbThermalPrinter.getVersion();
            } else if (!openFlag) {
                throw new DeviceNotOpenException();
            } else {
                ver = null;
                byte[] version = new byte[color];
                if (getFileContent(FILE_NAME) == null || !getFileContent(FILE_NAME).equals("SY581")) {
                    int ret = get_version(version);
                    if (ret != 0) {
                        throw getException(ret);
                    }
                    try {
                        String ver2 = new String(version, 1, version[0], "UTF-8");
                        try {
                            Log.d("idcard demo", "printer version:" + ver2);
                            ver = ver2;
                        } catch (UnsupportedEncodingException e) {
                            e = e;
                            ver = ver2;
                            e.printStackTrace();
                            return ver;
                        }
                    } catch (UnsupportedEncodingException e2) {
                        e = e2;
                        e.printStackTrace();
                        return ver;
                    }
                } else {
                    ver = ThermalPrinterSY581.getVersion();
                }
            }
        }
        return ver;
    }

    public static synchronized void printLogo(Bitmap image, int mode) throws TelpoException {
        int printWidth;
        int initWidth;
        int ret;
        int printHeight;
        int printWidth2;
        int initWidth2;
        int height;
        synchronized (ThermalPrinter.class) {
            if (!openFlag) {
                throw new DeviceNotOpenException("The printer has not been init!");
            } else if (image == null) {
                throw new NullPointerException();
            } else {
                int printer_type = SystemUtil.getPrinterType();
                if (Build.MODEL.equals("MTDP-618A") || Build.MODEL.equals("TPS650M")) {
                    printer_type = 2;
                }
                if (printer_type == 7) {
                    int img_height = image.getHeight();
                    int img_width = image.getWidth();
                    int image_col_bytes = (img_width + 7) >> 3;
                    byte[] buffer = new byte[((img_height * image_col_bytes) + 8 + 3)];
                    buffer[0] = 27;
                    buffer[1] = 97;
                    if (mode == 1) {
                        buffer[2] = 1;
                    } else if (mode == 2) {
                        buffer[2] = 2;
                    } else {
                        buffer[2] = 0;
                    }
                    buffer[3] = 29;
                    buffer[4] = 118;
                    buffer[5] = 48;
                    buffer[6] = 0;
                    buffer[7] = (byte) image_col_bytes;
                    buffer[8] = (byte) (image_col_bytes >> 8);
                    buffer[9] = (byte) img_height;
                    buffer[10] = (byte) (img_height >> 8);
                    int ss = 11;
                    int row_index = 0;
                    while (row_index < img_height) {
                        int col_index = 0;
                        int h = 0;
                        int ss2 = ss;
                        while (col_index < image_col_bytes) {
                            int temp = 0;
                            int i = 0;
                            while (true) {
                                if (i >= 8) {
                                    break;
                                } else if (i + h >= img_width) {
                                    temp <<= 8 - i;
                                    break;
                                } else {
                                    int Pixel_val = image.getPixel(i + h, row_index);
                                    if (((ARGB_MASK_RED & Pixel_val) >> 16) <= color || ((ARGB_MASK_GREEN & Pixel_val) >> 8) <= color || (Pixel_val & ARGB_MASK_BLUE) <= color) {
                                        temp = (temp << 1) + 1;
                                    } else {
                                        temp <<= 1;
                                    }
                                    i++;
                                }
                            }
                            buffer[ss2] = (byte) temp;
                            col_index++;
                            h += 8;
                            ss2++;
                        }
                        row_index++;
                        ss = ss2;
                    }
                    ret = print_logo(buffer.length, 0, buffer);
                } else if (printer_type == 6) {
                    Bitmap bitmap = adjustBitmap(image, mode);
                    int img_height2 = bitmap.getHeight();
                    int img_width2 = bitmap.getWidth();
                    int image_row_bytes = (img_height2 + 7) >> 3;
                    byte[] bitmap_mode_cmd = {27, 42, 1, (byte) (img_width2 & ARGB_MASK_BLUE), (byte) ((img_width2 >> 8) & ARGB_MASK_BLUE)};
                    byte[] buffer2 = new byte[((img_width2 * image_row_bytes) + image_row_bytes + (bitmap_mode_cmd.length * image_row_bytes) + 6 + 3 + 2)];
                    buffer2[0] = 29;
                    buffer2[1] = 69;
                    buffer2[2] = 14;
                    buffer2[3] = 27;
                    buffer2[4] = 51;
                    buffer2[5] = 0;
                    int row_index2 = 0;
                    int h2 = 0;
                    int ss3 = 6;
                    while (row_index2 < image_row_bytes) {
                        System.arraycopy(bitmap_mode_cmd, 0, buffer2, ss3, bitmap_mode_cmd.length);
                        int col_index2 = 0;
                        int ss4 = ss3 + bitmap_mode_cmd.length;
                        while (col_index2 < img_width2) {
                            int temp2 = 0;
                            int i2 = 0;
                            while (i2 < 8 && i2 + h2 < img_height2) {
                                int Pixel_val2 = bitmap.getPixel(col_index2, i2 + h2);
                                if (((ARGB_MASK_RED & Pixel_val2) >> 16) <= color || ((ARGB_MASK_GREEN & Pixel_val2) >> 8) <= color || (Pixel_val2 & ARGB_MASK_BLUE) <= color) {
                                    temp2 = (temp2 << 1) + 1;
                                } else {
                                    temp2 <<= 1;
                                }
                                i2++;
                            }
                            buffer2[ss4] = (byte) temp2;
                            col_index2++;
                            ss4++;
                        }
                        buffer2[ss4] = 10;
                        row_index2++;
                        h2 += 8;
                        ss3 = ss4 + 1;
                    }
                    int ss5 = ss3 + 1;
                    buffer2[ss3] = 29;
                    int ss6 = ss5 + 1;
                    buffer2[ss5] = 69;
                    int ss7 = ss6 + 1;
                    buffer2[ss6] = 1;
                    int ss8 = ss7 + 1;
                    buffer2[ss7] = 27;
                    int i3 = ss8 + 1;
                    buffer2[ss8] = 64;
                    ret = print_logo(buffer2.length, 0, buffer2);
                } else if (printer_type != 5 && printer_type != 3 && printer_type != 4) {
                    if (image.getHeight() % 8 != 0) {
                        printHeight = ((image.getHeight() / 8) + 1) * 8;
                    } else {
                        printHeight = image.getHeight();
                    }
                    if (image.getWidth() > 384) {
                        throw new IllegalArgumentException("The width or the height of the image to print is illegal!");
                    }
                    switch (mode) {
                        case 0:
                            printWidth2 = image.getWidth();
                            initWidth2 = 0;
                            break;
                        case 1:
                            printWidth2 = ((384 - image.getWidth()) / 2) + image.getWidth();
                            initWidth2 = (384 - image.getWidth()) / 2;
                            break;
                        case 2:
                            printWidth2 = 384;
                            initWidth2 = 384 - image.getWidth();
                            break;
                        default:
                            throw new IllegalArgumentException("The mode algin of the image to print is illegal!");
                    }
                    byte[] Imagelogo = new byte[((printWidth2 * printHeight) / 8)];
                    Log.e(TAG, ":" + initWidth2 + ":" + printWidth2 + ":" + printHeight);
                    int ss9 = initWidth2;
                    if (image.getConfig().equals(Bitmap.Config.ARGB_8888)) {
                        for (int row = 0; row < printHeight / 8; row++) {
                            for (int width = initWidth2; width < image.getWidth() + initWidth2; width++) {
                                int temp3 = 0;
                                for (int height2 = row * 8; height2 < Math.min((row * 8) + 8, image.getHeight()); height2++) {
                                    int pix = image.getPixel(width - initWidth2, height2);
                                    if (((ARGB_MASK_RED & pix) >> 16) <= color || ((ARGB_MASK_GREEN & pix) >> 8) <= color || (pix & ARGB_MASK_BLUE) <= color) {
                                        temp3 = (temp3 << 1) + 1;
                                    } else {
                                        temp3 <<= 1;
                                    }
                                }
                                Imagelogo[ss9] = (byte) temp3;
                                ss9++;
                            }
                            ss9 += initWidth2;
                        }
                        Log.i(TAG, "dealing ARGB_8888 image");
                    } else {
                        if (image.getConfig().equals(Bitmap.Config.ALPHA_8)) {
                            for (int row2 = 0; row2 < printHeight / 8; row2++) {
                                for (int width2 = initWidth2; width2 < image.getWidth() + initWidth2; width2++) {
                                    int temp4 = 0;
                                    for (int height3 = row2 * 8; height3 < Math.min((row2 * 8) + 8, image.getHeight()); height3++) {
                                        if ((image.getPixel(width2 - initWidth2, height3) & ARGB_MASK_BLUE) > color) {
                                            temp4 = (temp4 * 2) << 1;
                                        } else {
                                            temp4 = (temp4 << 1) + 1;
                                        }
                                    }
                                    Imagelogo[ss9] = (byte) temp4;
                                    ss9++;
                                }
                                ss9 += initWidth2;
                            }
                            Log.i(TAG, "dealing ALPHA_8 image");
                        } else {
                            if (image.getConfig().equals(Bitmap.Config.RGB_565)) {
                                for (int row3 = 0; row3 < printHeight / 8; row3++) {
                                    for (int height4 = row3 * 8; height4 < Math.min((row3 * 8) + 8, image.getHeight()); height4 = height + 1) {
                                        int temp5 = 0;
                                        height = row3 * 8;
                                        while (height < (row3 * 8) + 8) {
                                            int pix2 = image.getPixel(0 - initWidth2, height);
                                            if (((RGB565_MASK_RED & pix2) >> 11) <= 15 || ((pix2 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix2 & RGB565_MASK_BLUE) <= 15) {
                                                temp5 = (temp5 << 1) + 1;
                                            } else {
                                                temp5 <<= 1;
                                            }
                                            height++;
                                        }
                                        Imagelogo[ss9] = (byte) temp5;
                                        ss9++;
                                    }
                                    ss9 += initWidth2;
                                }
                                Log.i(TAG, "dealing RGB_565 image");
                            } else {
                                Log.e(TAG, "unsupport image formate!");
                            }
                        }
                    }
                    ret = print_logo(printWidth2, printHeight, Imagelogo);
                } else if (image.getWidth() > 576 || image.getHeight() < 1) {
                    throw new IllegalArgumentException("The width or the height of the image to print is illegal!");
                } else {
                    int widthLeft = image.getWidth() % 8;
                    if (widthLeft != 0) {
                        printWidth = (image.getWidth() - widthLeft) + 8;
                    } else {
                        printWidth = image.getWidth();
                    }
                    switch (mode) {
                        case 0:
                            initWidth = 0;
                            break;
                        case 1:
                            initWidth = (576 - printWidth) / 2;
                            int temp6 = initWidth % 8;
                            if (temp6 != 0) {
                                initWidth = (initWidth - temp6) + 8;
                            }
                            printWidth += initWidth;
                            break;
                        case 2:
                            initWidth = 576 - printWidth;
                            printWidth = 576;
                            break;
                        default:
                            throw new IllegalArgumentException("The mode algin of the image to print is illegal!");
                    }
                    Log.i(TAG, "printWidth: " + printWidth);
                    byte[] Imagelogo2 = new byte[((printWidth / 8) * image.getHeight())];
                    int initWidth3 = initWidth / 8;
                    int ss10 = initWidth3;
                    if (image.getConfig().equals(Bitmap.Config.ARGB_8888)) {
                        int widthOctet = image.getWidth() / 8;
                        for (int row4 = 0; row4 < image.getHeight(); row4++) {
                            int width3 = 0;
                            int column = 0;
                            while (width3 < widthOctet) {
                                int temp7 = 0;
                                for (int i4 = 0; i4 < 8; i4++) {
                                    int pix3 = image.getPixel(i4 + column, row4);
                                    if (((ARGB_MASK_RED & pix3) >> 16) <= color || ((ARGB_MASK_GREEN & pix3) >> 8) <= color || (pix3 & ARGB_MASK_BLUE) <= color) {
                                        temp7 = (temp7 << 1) + 1;
                                    } else {
                                        temp7 <<= 1;
                                    }
                                }
                                Imagelogo2[ss10] = (byte) temp7;
                                ss10++;
                                width3++;
                                column += 8;
                            }
                            if (widthLeft != 0) {
                                int temp8 = 0;
                                for (int i5 = 0; i5 < widthLeft; i5++) {
                                    int pix4 = image.getPixel(i5 + column, row4);
                                    if (((ARGB_MASK_RED & pix4) >> 16) <= color || ((ARGB_MASK_GREEN & pix4) >> 8) <= color || (pix4 & ARGB_MASK_BLUE) <= color) {
                                        temp8 = (temp8 << 1) + 1;
                                    } else {
                                        temp8 <<= 1;
                                    }
                                }
                                Imagelogo2[ss10] = (byte) (temp8 << (8 - widthLeft));
                                ss10++;
                            }
                            ss10 += initWidth3;
                        }
                        Log.i(TAG, "dealing ARGB_8888 image");
                    } else {
                        if (image.getConfig().equals(Bitmap.Config.ALPHA_8)) {
                            int widthOctet2 = image.getWidth() / 8;
                            for (int row5 = 0; row5 < image.getHeight(); row5++) {
                                int width4 = 0;
                                int column2 = 0;
                                while (width4 < widthOctet2) {
                                    int temp9 = 0;
                                    for (int i6 = 0; i6 < 8; i6++) {
                                        if ((image.getPixel(i6 + column2, row5) & ARGB_MASK_BLUE) > color) {
                                            temp9 <<= 1;
                                        } else {
                                            temp9 = (temp9 << 1) + 1;
                                        }
                                    }
                                    Imagelogo2[ss10] = (byte) temp9;
                                    ss10++;
                                    width4++;
                                    column2 += 8;
                                }
                                if (widthLeft != 0) {
                                    int temp10 = 0;
                                    for (int i7 = 0; i7 < widthLeft; i7++) {
                                        if ((image.getPixel(i7 + column2, row5) & ARGB_MASK_BLUE) > color) {
                                            temp10 <<= 1;
                                        } else {
                                            temp10 = (temp10 << 1) + 1;
                                        }
                                    }
                                    Imagelogo2[ss10] = (byte) (temp10 << (8 - widthLeft));
                                    ss10++;
                                }
                                ss10 += initWidth3;
                            }
                            Log.i(TAG, "dealing ALPHA_8 image");
                        } else {
                            if (image.getConfig().equals(Bitmap.Config.RGB_565)) {
                                int widthOctet3 = image.getWidth() / 8;
                                for (int row6 = 0; row6 < image.getHeight(); row6++) {
                                    int width5 = 0;
                                    int column3 = 0;
                                    while (width5 < widthOctet3) {
                                        int temp11 = 0;
                                        for (int i8 = 0; i8 < 8; i8++) {
                                            int pix5 = image.getPixel(i8 + column3, row6);
                                            if (((RGB565_MASK_RED & pix5) >> 11) <= 15 || ((pix5 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix5 & RGB565_MASK_BLUE) <= 15) {
                                                temp11 = (temp11 << 1) + 1;
                                            } else {
                                                temp11 <<= 1;
                                            }
                                        }
                                        Imagelogo2[ss10] = (byte) temp11;
                                        ss10++;
                                        width5++;
                                        column3 += 8;
                                    }
                                    if (widthLeft != 0) {
                                        int temp12 = 0;
                                        for (int i9 = 0; i9 < widthLeft; i9++) {
                                            int pix6 = image.getPixel(i9 + column3, row6);
                                            if (((RGB565_MASK_RED & pix6) >> 11) <= 15 || ((pix6 & RGB565_MASK_GREEN) >> 5) <= 30 || (pix6 & RGB565_MASK_BLUE) <= 15) {
                                                temp12 = (temp12 << 1) + 1;
                                            } else {
                                                temp12 <<= 1;
                                            }
                                        }
                                        Imagelogo2[ss10] = (byte) (temp12 << (8 - widthLeft));
                                        ss10++;
                                    }
                                    ss10 += initWidth3;
                                }
                                Log.i(TAG, "dealing RGB_565 image");
                            } else {
                                Log.e(TAG, "unsupport image formate!");
                            }
                        }
                    }
                    ret = print_logo(printWidth, image.getHeight(), Imagelogo2);
                }
                if (ret != 0) {
                    throw getException(ret);
                }
            }
        }
    }

    private static Bitmap CreateCode(String str, BarcodeFormat type, int bmpWidth, int bmpHeight) throws InternalErrorException {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[(width * height)];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[(y * width) + x] = -16777216;
                    } else {
                        pixels[(y * width) + x] = -1;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            throw new InternalErrorException("Failed to encode bitmap");
        }
    }

    public static void searchMark(int search_distance, int walk_distance) throws TelpoException {
        if (mUsbThermalPrinter != null) {
            mUsbThermalPrinter.searchMark(search_distance, walk_distance);
        } else if (!openFlag) {
            throw new DeviceNotOpenException();
        } else {
            int ret = search_mark(0, search_distance, walk_distance);
            if (ret != 0) {
                throw getException(ret);
            }
        }
    }

    public static void paperCut() throws TelpoException {
        if (mUsbThermalPrinter != null) {
            mUsbThermalPrinter.paperCut();
        } else if (!openFlag) {
            throw new DeviceNotOpenException();
        } else {
            int ret = paper_cut();
            if (ret != 0) {
                throw getException(ret);
            }
        }
    }

    public static void paperCutAll() throws TelpoException {
        if (!openFlag) {
            throw new DeviceNotOpenException();
        }
        int ret = paper_cut_all();
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void sendCommand(String cmdStr) throws TelpoException {
        if (!openFlag) {
            throw new DeviceNotOpenException();
        } else if (cmdStr == null) {
            throw new IllegalArgumentException();
        } else {
            byte[] cmd = str2BCD(cmdStr.replace(" ", ""));
            int ret = send_command(cmd, cmd.length);
            if (ret != 0) {
                throw getException(ret);
            }
        }
    }

    public static void sendCommand(byte[] cmdStr, int len) throws TelpoException {
        if (!openFlag) {
            throw new DeviceNotOpenException();
        } else if (cmdStr == null) {
            throw new IllegalArgumentException();
        } else {
            int ret = send_command(cmdStr, len);
            if (ret != 0) {
                throw getException(ret);
            }
        }
    }

    public static void setBold(boolean isBold) throws TelpoException {
        if (!openFlag) {
            throw new DeviceNotOpenException();
        } else if (isBold) {
            set_bold(1);
        } else {
            set_bold(0);
        }
    }

    private static byte[] str2BCD(String string) {
        String str;
        int len;
        int len2 = string.length();
        if (len2 % 2 == 1) {
            str = String.valueOf(string) + "0";
            len = (len2 + 1) >> 1;
        } else {
            str = string;
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

    private static int getGreyLevel(int pixel, float intensity) {
        float red = (float) Color.red(pixel);
        float green = (float) Color.green(pixel);
        int gray = (int) (((float) (((double) ((red + green) + ((float) Color.blue(pixel)))) / 3.0d)) * intensity);
        if (gray > ARGB_MASK_BLUE) {
            return ARGB_MASK_BLUE;
        }
        return gray;
    }

    private static Bitmap adjustBitmap(Bitmap bitmap, int align) {
        if (bitmap == null) {
            return null;
        }
        int adjustWidth = bitmap.getWidth();
        int adjustHeight = bitmap.getHeight();
        int offset = 0;
        if (align == 1) {
            offset = (384 - adjustWidth) / 2;
            adjustWidth += offset;
            int temp = adjustWidth % 8;
            if (temp != 0) {
                adjustWidth += 8 - temp;
            }
        } else if (align == 2) {
            offset = 384 - adjustWidth;
            adjustWidth = 384;
        } else {
            int temp2 = adjustWidth % 8;
            if (temp2 != 0) {
                adjustWidth += 8 - temp2;
            }
        }
        Bitmap newBitmap = Bitmap.createBitmap(adjustWidth, adjustHeight, bitmap.getConfig());
        Paint paint = new Paint();
        paint.setColor(-1);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawRect(0.0f, 0.0f, (float) adjustWidth, (float) adjustHeight, paint);
        canvas.drawBitmap(bitmap, (float) offset, 0.0f, (Paint) null);
        return newBitmap;
    }

    private static String getFileContent(String file_name) {
        String fileContent = null;
        try {
            File file = new File(file_name);
            if (!file.isFile() || !file.exists()) {
                Log.e("idcard demo", "can not find file");
                file.createNewFile();
                return fileContent;
            }
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(isr);
            while (true) {
                String lineTxt = br.readLine();
                if (lineTxt == null) {
                    break;
                }
                fileContent = lineTxt;
            }
            isr.close();
            br.close();
            return fileContent;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
