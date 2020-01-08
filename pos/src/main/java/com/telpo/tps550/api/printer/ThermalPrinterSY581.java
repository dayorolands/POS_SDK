package com.telpo.tps550.api.printer;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android_serialport_api.SerialPort;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ThermalPrinterSY581 {
    public static final int ALGIN_LEFT = 0;
    public static final int ALGIN_MIDDLE = 1;
    public static final int ALGIN_RIGHT = 2;
    private static final int ARGB_MASK_BLUE = 255;
    private static final int ARGB_MASK_GREEN = 65280;
    private static final int ARGB_MASK_RED = 16711680;
    private static final int RGB565_MASK_BLUE = 31;
    private static final int RGB565_MASK_GREEN = 2016;
    private static final int RGB565_MASK_RED = 63488;
    /* access modifiers changed from: private */
    public static InputStream checkInputStream = null;
    private static ReadThread checkReadThread = null;
    private static SerialPort checkSerialPort = null;
    private static final int color = 128;
    static int count = 0;

    static class ReadThread extends Thread {
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    byte[] buffer = new byte[64];
                    if (ThermalPrinterSY581.checkInputStream.read(buffer) > 0) {
                        byte name = buffer[0];
                        if (name == 17) {
                            Log.d("idcard demo", "继续发送");
                        } else if (name == 19) {
                            Log.d("idcard demo", "停止发送");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public static synchronized void openCheckThread() {
        synchronized (ThermalPrinterSY581.class) {
            try {
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS650T.ordinal()) {
                    checkSerialPort = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS0"), 460800, 0);
                } else {
                    checkSerialPort = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS4"), 460800, 0);
                }
                checkInputStream = checkSerialPort.getInputStream();
            } catch (SecurityException e1) {
                e1.printStackTrace();
            } catch (IOException e12) {
                e12.printStackTrace();
            }
            checkReadThread = new ReadThread();
            checkReadThread.start();
        }
        return;
    }

    public static synchronized void closeCheckThread() {
        synchronized (ThermalPrinterSY581.class) {
            if (checkReadThread != null) {
                checkReadThread.interrupt();
            }
            if (checkSerialPort != null) {
                checkSerialPort.close();
                checkSerialPort = null;
            }
            checkSerialPort = null;
            try {
                checkInputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static synchronized void reset() {
        synchronized (ThermalPrinterSY581.class) {
            sendCommand(new byte[]{27, 121});
        }
    }

    public static synchronized void checkStatus() {
        synchronized (ThermalPrinterSY581.class) {
            final byte[] cmdStr = {27, 22};
            new Thread(new Runnable() {
                public void run() {
                    ThermalPrinterSY581.sendCommand(cmdStr);
                }
            }).start();
        }
    }

    public static synchronized void setLineSpace(int lineSpace) {
        String lineSpaceStr;
        synchronized (ThermalPrinterSY581.class) {
            if (lineSpace == 1) {
                lineSpaceStr = "01";
            } else if (lineSpace == 2) {
                lineSpaceStr = "02";
            } else if (lineSpace == 3) {
                lineSpaceStr = "03";
            } else if (lineSpace == 4) {
                lineSpaceStr = "04";
            } else if (lineSpace == 5) {
                lineSpaceStr = "05";
            } else if (lineSpace == 6) {
                lineSpaceStr = "06";
            } else if (lineSpace == 7) {
                lineSpaceStr = "07";
            } else if (lineSpace == 8) {
                lineSpaceStr = "08";
            } else if (lineSpace == 9) {
                lineSpaceStr = "09";
            } else {
                lineSpaceStr = new StringBuilder().append(lineSpace).toString();
            }
            sendCommand(new byte[]{27, 51, parseHexStr2Byte(lineSpaceStr)[0]});
        }
    }

    public static synchronized void setGray(int gray) {
        synchronized (ThermalPrinterSY581.class) {
            String grayStr = "";
            if (gray == 0) {
                grayStr = "08";
            } else if (gray == 1) {
                grayStr = "07";
            } else if (gray == 2) {
                grayStr = "06";
            } else if (gray == 3) {
                grayStr = "05";
            } else if (gray == 4) {
                grayStr = "04";
            } else if (gray == 5) {
                grayStr = "03";
            }
            sendCommand(new byte[]{27, 115, parseHexStr2Byte(grayStr)[0]});
        }
    }

    public static synchronized void setAlign(int mode) {
        synchronized (ThermalPrinterSY581.class) {
            byte[] cmdStr = null;
            if (mode == 0) {
                byte[] cmdStr2 = new byte[3];
                cmdStr2[0] = 27;
                cmdStr2[1] = 97;
                cmdStr = cmdStr2;
            } else if (mode == 1) {
                cmdStr = new byte[]{27, 97, 1};
            } else if (mode == 2) {
                cmdStr = new byte[]{27, 97, 2};
            }
            sendCommand(cmdStr);
        }
    }

    public static synchronized String getVersion() {
        synchronized (ThermalPrinterSY581.class) {
            sendCommand(new byte[]{27, 119});
        }
        return "";
    }

    public static synchronized void setTem(int tem) {
        synchronized (ThermalPrinterSY581.class) {
            sendCommand(new byte[]{29, 40, StringUtil.toBytes(Integer.toHexString(tem))[0]});
        }
    }

    public static synchronized void setLeftDistance(int leftDistance) {
        synchronized (ThermalPrinterSY581.class) {
            if (leftDistance >= 0 && leftDistance <= ARGB_MASK_BLUE) {
                String nL_str_16 = Integer.toHexString(leftDistance);
                if (nL_str_16.length() == 1) {
                    nL_str_16 = "0" + nL_str_16;
                }
                byte nL = parseHexStr2Byte(nL_str_16)[0];
                byte[] cmdStr = new byte[4];
                cmdStr[0] = 27;
                cmdStr[1] = 108;
                cmdStr[2] = nL;
                sendCommand(cmdStr);
            }
        }
    }

    public static synchronized void setFont(int multiple) {
        synchronized (ThermalPrinterSY581.class) {
            if (multiple >= 0 && multiple <= 7) {
                if (multiple == 0) {
                    setFont(0, 0);
                } else if (multiple == 1) {
                    setFont(1, 1);
                } else if (multiple == 2) {
                    setFont(2, 2);
                } else if (multiple == 3) {
                    setFont(3, 3);
                } else if (multiple == 4) {
                    setFont(4, 4);
                } else if (multiple == 5) {
                    setFont(5, 5);
                } else if (multiple == 6) {
                    setFont(6, 6);
                } else if (multiple == 7) {
                    setFont(7, 7);
                }
            }
        }
    }

    public static synchronized void setFont(int widthMultiple, int heightMultiple) {
        String fontSize;
        byte[] cmdStr;
        synchronized (ThermalPrinterSY581.class) {
            if (heightMultiple >= 0 && heightMultiple <= 7 && widthMultiple >= 0 && widthMultiple <= 7) {
                if (widthMultiple == 0) {
                    widthMultiple = 0;
                } else if (widthMultiple == 1) {
                    widthMultiple = 10;
                } else if (widthMultiple == 2) {
                    widthMultiple = 20;
                } else if (widthMultiple == 3) {
                    widthMultiple = 30;
                } else if (widthMultiple == 4) {
                    widthMultiple = 40;
                } else if (widthMultiple == 5) {
                    widthMultiple = 50;
                } else if (widthMultiple == 6) {
                    widthMultiple = 60;
                } else if (widthMultiple == 7) {
                    widthMultiple = 70;
                }
                if (widthMultiple == 0) {
                    fontSize = "0" + heightMultiple;
                } else {
                    fontSize = new StringBuilder().append(heightMultiple).append(widthMultiple).toString();
                }
                byte font = parseHexStr2Byte(fontSize)[0];
                if (fontSize.equals("00")) {
                    cmdStr = new byte[3];
                    cmdStr[0] = 28;
                    cmdStr[1] = 33;
                } else {
                    cmdStr = new byte[]{29, 33, font};
                }
                sendCommand(cmdStr);
            }
        }
    }

    public static synchronized void printLogo(Bitmap bitmap) {
        Bitmap picture;
        synchronized (ThermalPrinterSY581.class) {
            SerialPort serial = null;
            byte[] cmdStr = null;
            int count2 = 0;
            try {
                serial = SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS650T.ordinal() ? new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS0"), 460800, 0) : new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS4"), 460800, 0);
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
            Log.d("idcard demo", "cmd length:" + cmdStr.length);
            while (cmdStr.length - count2 > 100) {
                try {
                    mOutputStream.write(cmdStr, count2, 100);
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e4) {
                        e4.printStackTrace();
                    }
                    Log.d("idcard demo", "write:" + count2);
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
                count2 += 100;
            }
            try {
                mOutputStream.write(cmdStr, count2, cmdStr.length - count2);
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

    private static synchronized Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        Bitmap newbm;
        synchronized (ThermalPrinterSY581.class) {
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
        synchronized (ThermalPrinterSY581.class) {
            int ss = 0;
            if (image == null) {
                throw new NullPointerException();
            }
            int printer_type = SystemUtil.checkPrinter581();
            if (printer_type == 5 || printer_type == 3 || printer_type == 4 || printer_type == 8) {
                Log.d("idcard demo", "img_width:" + image.getWidth());
                Log.d("idcard demo", "img_height:" + image.getHeight());
                Log.d("idcard demo", "picture config is:" + image.getConfig());
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
                Log.d("idcard demo", "imagelogo length:" + Imagelogo2.length);
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
                    Log.i("idcard demo", "dealing ARGB_8888 image");
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
                        Log.i("idcard demo", "dealing ALPHA_8 image");
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
                            Log.i("idcard demo", "dealing RGB_565 image");
                            byte[] bArr3 = Imagelogo2;
                            Imagelogo = Imagelogo2;
                        } else {
                            Log.e("idcard demo", "unsupport image formate!");
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
                    Log.i("idcard demo", "dealing ARGB_8888 image");
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
                        Log.i("idcard demo", "dealing ALPHA_8 image");
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
                            Log.i("idcard demo", "dealing RGB_565 image");
                        } else {
                            Log.e("idcard demo", "unsupport image formate!");
                        }
                    }
                }
                byte[] bArr5 = Imagelogo3;
                Imagelogo = Imagelogo3;
            }
        }
        return Imagelogo;
    }

    private static synchronized byte[] parseHexStr2Byte(String hexStr) {
        byte[] result;
        synchronized (ThermalPrinterSY581.class) {
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

    /* access modifiers changed from: private */
    public static synchronized void sendCommand(byte[] cmdStr) {
        SerialPort serial;
        synchronized (ThermalPrinterSY581.class) {
            SerialPort serial2 = null;
            OutputStream mOutputStream = null;
            try {
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS650T.ordinal()) {
                    serial = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS0"), 460800, 0);
                } else {
                    serial = new SerialPort(true,"/system/xbin/su",new File("/dev/ttyS4"), 460800, 0);
                }
                OutputStream mOutputStream2 = serial.getOutputStream();
                for (byte write : cmdStr) {
                    mOutputStream2.write(write);
                }
                if (mOutputStream2 != null) {
                    try {
                        mOutputStream2.close();
                    } catch (IOException e) {
                    }
                }
                if (serial != null) {
                    serial.close();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            } catch (FileNotFoundException e3) {
                e3.printStackTrace();
                if (mOutputStream != null) {
                    try {
                        mOutputStream.close();
                    } catch (IOException e4) {
                    }
                }
                if (serial2 != null) {
                    serial2.close();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e5) {
                    e5.printStackTrace();
                }
            } catch (SecurityException e6) {
                e6.printStackTrace();
                if (mOutputStream != null) {
                    try {
                        mOutputStream.close();
                    } catch (IOException e7) {
                    }
                }
                if (serial2 != null) {
                    serial2.close();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e8) {
                    e8.printStackTrace();
                }
            } catch (IOException e9) {
                e9.printStackTrace();
                if (mOutputStream != null) {
                    try {
                        mOutputStream.close();
                    } catch (IOException e10) {
                    }
                }
                if (serial2 != null) {
                    serial2.close();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e11) {
                    e11.printStackTrace();
                }
            } catch (Throwable th) {
                if (mOutputStream != null) {
                    try {
                        mOutputStream.close();
                    } catch (IOException e12) {
                    }
                }
                if (serial2 != null) {
                    serial2.close();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e13) {
                    e13.printStackTrace();
                }
                throw th;
            }
        }
        return;
    }
}
