package com.telpo.tps550.api.printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.telpo.tps550.api.DeviceAlreadyOpenException;
import com.telpo.tps550.api.DeviceNotOpenException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.iccard.NotEnoughBufferException;
import com.telpo.tps550.api.printer.StyleConfig;
import com.telpo.tps550.api.util.ShellUtils;
import com.telpo.tps550.api.util.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Printer {
    private static final int ACTION_CMD = 5;
    private static final int ACTION_LED = 2;
    private static final int ACTION_MARK = 4;
    private static final int ACTION_PRINT = 0;
    private static final int ACTION_STATUS = 1;
    private static final int ACTION_VERSION = 3;
    public static final int ERROR_LED = 7;
    public static final int LED_OFF = 0;
    public static final int LED_ON = 1;
    private static final int MODE_BAR = 2;
    private static final int MODE_PIC = 1;
    private static final int MODE_TEXT = 0;
    public static final int NO_PAPER_LED = 4;
    public static final int POWER_LED = 1;
    public static final int STATUS_DISCONNECT = -1004;
    public static final int STATUS_NO_PAPER = -1001;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OVER_FLOW = -1003;
    public static final int STATUS_OVER_HEAT = -1002;
    public static final int STATUS_UNKNOWN = -9999;
    private static final String TAG = "Printer";
    public static final int WORKING_LED = 2;
    private static int barcode_mode = -1;
    private static Handler handler = null;
    private static HandlerThread handlerThread = null;
    /* access modifiers changed from: private */
    public static Object lock = new Object();
    /* access modifiers changed from: private */
    public static int mStatus = STATUS_UNKNOWN;
    /* access modifiers changed from: private */
    public static String mVersion = null;
    private static List<PrintItem> printList = null;

    private static class PrintItem {
        public Bitmap bitmap = null;
        public int feed = 0;
        public int mode = 0;
        public String string = null;
        public StyleConfig styleConfig = null;

        public PrintItem(String string2, StyleConfig styleConfig2) {
            this.string = string2;
            this.styleConfig = styleConfig2;
        }

        public PrintItem(Bitmap bitmap2, StyleConfig styleConfig2) {
            this.styleConfig = styleConfig2;
            this.bitmap = bitmap2;
            this.mode = 1;
        }
    }

    public static synchronized void printText(String txt, StyleConfig style) {
        synchronized (Printer.class) {
            if (txt != null) {
                if (printList != null) {
                    StyleConfig styleConfig = new StyleConfig();
                    if (style != null) {
                        styleConfig.fontFamily = style.fontFamily;
                        styleConfig.fontSize = style.fontSize;
                        styleConfig.fontStyle = style.fontStyle;
                        styleConfig.align = style.align;
                        styleConfig.gray = style.gray;
                        styleConfig.lineSpace = style.lineSpace;
                        styleConfig.newLine = style.newLine;
                    }
                    printList.add(new PrintItem(txt, styleConfig));
                }
            }
        }
    }

    public static synchronized void printBarCode(String barcode, StyleConfig.Align align) {
        synchronized (Printer.class) {
            if (barcode != null) {
                if (printList != null) {
                    if (barcode_mode < 0) {
                        barcode_mode = 0;
                        try {
                            String version = ThermalPrinter.getVersion().trim();
                            if (version.substring(version.length() - 8).compareTo("20151106") >= 0) {
                                barcode_mode = 1;
                            }
                        } catch (TelpoException e) {
                            e.printStackTrace();
                        }
                    }
                    if (barcode_mode == 1) {
                        StyleConfig styleConfig = new StyleConfig();
                        styleConfig.align = align;
                        PrintItem printItem = new PrintItem(barcode, styleConfig);
                        printItem.mode = 2;
                        printList.add(printItem);
                    } else {
                        try {
                            printList.add(new PrintItem(adjustBitmap(CreateCode(barcode, BarcodeFormat.CODE_128, 360, 64), align), new StyleConfig()));
                        } catch (WriterException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
        return;
    }

    public static synchronized void printQRCode(String QRCode, StyleConfig.Align align) {
        synchronized (Printer.class) {
            if (QRCode != null) {
                if (printList != null) {
                    try {
                        Bitmap bitmap = CreateCode(QRCode, BarcodeFormat.QR_CODE, 256, 256);
                        printList.add(new PrintItem(adjustBitmap(Bitmap.createBitmap(bitmap, 40, 40, bitmap.getWidth() - 80, bitmap.getHeight() - 80), align), new StyleConfig()));
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return;
    }

    public static synchronized void printImage(String path, StyleConfig.Align align) {
        synchronized (Printer.class) {
            if (path != null) {
                if (printList != null) {
                    if (new File(path).exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        printList.add(new PrintItem(adjustBitmap(Bitmap.createBitmap(bitmap, 0, 16, bitmap.getWidth(), bitmap.getHeight() - 32), align), new StyleConfig()));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static void commitOperation(List<PrintItem> contentList, ICommitCallback commitCallback) {
        int status;
        String msg;
        int printFlag = 0;
        try {
            int status2 = ThermalPrinter.checkStatus();
            if (status2 != 0) {
                switch (status2) {
                    case 1:
                        status = STATUS_NO_PAPER;
                        msg = "Printer out of paper";
                        break;
                    case 2:
                        status = STATUS_OVER_HEAT;
                        msg = "Printer over heat";
                        break;
                    case 3:
                        status = STATUS_OVER_FLOW;
                        msg = "Printer over flow";
                        break;
                    default:
                        status = STATUS_UNKNOWN;
                        msg = "Printer error";
                        break;
                }
                printerReset();
                if (commitCallback != null) {
                    commitCallback.printerStatus(status, msg);
                    return;
                }
                return;
            }
        } catch (DeviceNotOpenException e) {
            if (commitCallback != null) {
                commitCallback.printerStatus(STATUS_DISCONNECT, "Printer disconnect");
                return;
            }
            return;
        } catch (TelpoException e1) {
            e1.printStackTrace();
        }
        for (PrintItem printItem : contentList) {
            if (printItem.mode == 1) {
                if (printFlag == 1) {
                    try {
                        ThermalPrinter.printString();
                        printFlag = 0;
                        Thread.sleep(200);
                    } catch (DeviceNotOpenException e2) {
                        e2.printStackTrace();
                        if (commitCallback != null) {
                            commitCallback.printerStatus(STATUS_DISCONNECT, "Printer disconnect");
                            return;
                        }
                        return;
                    } catch (NoPaperException e3) {
                        e3.printStackTrace();
                        printerReset();
                        if (commitCallback != null) {
                            commitCallback.printerStatus(STATUS_NO_PAPER, "Printer out of paper");
                            return;
                        }
                        return;
                    } catch (NotEnoughBufferException e4) {
                        e4.printStackTrace();
                        printerReset();
                        if (commitCallback != null) {
                            commitCallback.printerStatus(STATUS_OVER_FLOW, "Printer over flow");
                            return;
                        }
                        return;
                    } catch (OverHeatException e5) {
                        e5.printStackTrace();
                        printerReset();
                        if (commitCallback != null) {
                            commitCallback.printerStatus(STATUS_OVER_HEAT, "Printer over heat");
                            return;
                        }
                        return;
                    } catch (Exception e6) {
                        e6.printStackTrace();
                        printerReset();
                        if (commitCallback != null) {
                            commitCallback.printerStatus(STATUS_UNKNOWN, "Printer error");
                            return;
                        }
                        return;
                    }
                }
                ThermalPrinter.setAlgin(0);
                ThermalPrinter.setGray(printItem.styleConfig.gray);
                ThermalPrinter.printLogo(printItem.bitmap);
            } else {
                try {
                    if (printItem.styleConfig.fontSize == StyleConfig.FontSize.F1) {
                        ThermalPrinter.setFontSize(1);
                        ThermalPrinter.enlargeFontSize(1, 1);
                    } else if (printItem.styleConfig.fontSize == StyleConfig.FontSize.F3) {
                        ThermalPrinter.setFontSize(1);
                        ThermalPrinter.enlargeFontSize(2, 2);
                    } else if (printItem.styleConfig.fontSize == StyleConfig.FontSize.F4) {
                        ThermalPrinter.setFontSize(2);
                        ThermalPrinter.enlargeFontSize(2, 2);
                    } else {
                        ThermalPrinter.setFontSize(2);
                        ThermalPrinter.enlargeFontSize(1, 1);
                    }
                    if (printItem.styleConfig.align == StyleConfig.Align.CENTER) {
                        ThermalPrinter.setAlgin(1);
                    } else if (printItem.styleConfig.align == StyleConfig.Align.RIGHT) {
                        ThermalPrinter.setAlgin(2);
                    } else {
                        ThermalPrinter.setAlgin(0);
                    }
                    ThermalPrinter.setGray(printItem.styleConfig.gray);
                    ThermalPrinter.setLineSpace(printItem.styleConfig.lineSpace);
                    if (printItem.string.length() > 0) {
                        if (printItem.mode == 0) {
                            ThermalPrinter.addString(printItem.string);
                            if (printItem.styleConfig.newLine) {
                                ThermalPrinter.addString(ShellUtils.COMMAND_LINE_END);
                            }
                        } else if (printItem.mode == 2) {
                            ThermalPrinter.addBarcode(printItem.string);
                        }
                    }
                    if (printItem.feed > 0) {
                        ThermalPrinter.printStringAndWalk(0, 0, printItem.feed);
                        printFlag = 0;
                    } else {
                        printFlag = 1;
                    }
                } catch (DeviceNotOpenException e7) {
                    e7.printStackTrace();
                    if (commitCallback != null) {
                        commitCallback.printerStatus(STATUS_DISCONNECT, "Printer disconnect");
                        return;
                    }
                    return;
                } catch (NoPaperException e8) {
                    e8.printStackTrace();
                    printerReset();
                    if (commitCallback != null) {
                        commitCallback.printerStatus(STATUS_NO_PAPER, "Printer out of paper");
                        return;
                    }
                    return;
                } catch (NotEnoughBufferException e9) {
                    e9.printStackTrace();
                    printerReset();
                    if (commitCallback != null) {
                        commitCallback.printerStatus(STATUS_OVER_FLOW, "Printer over flow");
                        return;
                    }
                    return;
                } catch (OverHeatException e10) {
                    e10.printStackTrace();
                    printerReset();
                    if (commitCallback != null) {
                        commitCallback.printerStatus(STATUS_OVER_HEAT, "Printer over heat");
                        return;
                    }
                    return;
                } catch (Exception e11) {
                    e11.printStackTrace();
                    printerReset();
                    if (commitCallback != null) {
                        commitCallback.printerStatus(STATUS_UNKNOWN, "Printer error");
                        return;
                    }
                    return;
                }
            }
        }
        if (printFlag == 1) {
            try {
                ThermalPrinter.printString();
            } catch (DeviceNotOpenException e12) {
                e12.printStackTrace();
                if (commitCallback != null) {
                    commitCallback.printerStatus(STATUS_DISCONNECT, "Printer disconnect");
                    return;
                }
                return;
            } catch (NoPaperException e13) {
                e13.printStackTrace();
                printerReset();
                if (commitCallback != null) {
                    commitCallback.printerStatus(STATUS_NO_PAPER, "Printer out of paper");
                    return;
                }
                return;
            } catch (NotEnoughBufferException e14) {
                e14.printStackTrace();
                printerReset();
                if (commitCallback != null) {
                    commitCallback.printerStatus(STATUS_OVER_FLOW, "Printer over flow");
                    return;
                }
                return;
            } catch (OverHeatException e15) {
                e15.printStackTrace();
                printerReset();
                if (commitCallback != null) {
                    commitCallback.printerStatus(STATUS_OVER_HEAT, "Printer over heat");
                    return;
                }
                return;
            } catch (Exception e16) {
                e16.printStackTrace();
                printerReset();
                if (commitCallback != null) {
                    commitCallback.printerStatus(STATUS_UNKNOWN, "Printer error");
                    return;
                }
                return;
            }
        }
        if (commitCallback != null) {
            commitCallback.printerStatus(0, "Printer OK");
        }
    }

    public static synchronized void commitOperation() {
        synchronized (Printer.class) {
            if (!(printList == null || handler == null)) {
                List<PrintItem> list = new ArrayList<>(printList.size());
                for (PrintItem add : printList) {
                    list.add(add);
                }
                CommitData commitData = new CommitData((CommitData) null);
                commitData.printList = list;
                commitData.callback = null;
                handler.sendMessage(handler.obtainMessage(0, commitData));
                printList.clear();
            }
        }
    }

    public static synchronized void commitOperation(ICommitCallback callback) {
        synchronized (Printer.class) {
            if (!(printList == null || handler == null)) {
                List<PrintItem> list = new ArrayList<>(printList.size());
                for (PrintItem add : printList) {
                    list.add(add);
                }
                CommitData commitData = new CommitData((CommitData) null);
                commitData.printList = list;
                commitData.callback = callback;
                handler.sendMessage(handler.obtainMessage(0, commitData));
                printList.clear();
            }
        }
    }

    private static class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                CommitData data = (CommitData) msg.obj;
                Printer.commitOperation(data.printList, data.callback);
                try {
                    ThermalPrinter.clearString();
                    return;
                } catch (TelpoException e) {
                    e.printStackTrace();
                    return;
                }
            } else if (msg.what == 1) {
                synchronized (Printer.lock) {
                    try {
                        switch (ThermalPrinter.checkStatus()) {
                            case 0:
                                Printer.mStatus = 0;
                                break;
                            case 1:
                                Printer.mStatus = Printer.STATUS_NO_PAPER;
                                break;
                            case 2:
                                Printer.mStatus = Printer.STATUS_OVER_HEAT;
                                break;
                            case 3:
                                Printer.mStatus = Printer.STATUS_OVER_FLOW;
                                break;
                            default:
                                Printer.mStatus = Printer.STATUS_UNKNOWN;
                                break;
                        }
                    } catch (DeviceNotOpenException e2) {
                        Printer.mStatus = Printer.STATUS_DISCONNECT;
                    } catch (TelpoException e3) {
                        e3.printStackTrace();
                        Printer.mStatus = Printer.STATUS_UNKNOWN;
                    }
                    Printer.lock.notify();
                }
                return;
            } else if (msg.what == 2) {
                byte[] cmd = {27, 8, (byte) msg.arg1, (byte) msg.arg2};
                try {
                    ThermalPrinter.sendCommand(cmd, cmd.length);
                    return;
                } catch (TelpoException e4) {
                    e4.printStackTrace();
                    return;
                }
            } else if (msg.what == 3) {
                synchronized (Printer.lock) {
                    try {
                        Printer.mVersion = ThermalPrinter.getVersion();
                    } catch (TelpoException e5) {
                        e5.printStackTrace();
                    }
                    Printer.lock.notify();
                }
                return;
            } else if (msg.what == 4) {
                synchronized (Printer.lock) {
                    int[] obj = (int[]) msg.obj;
                    try {
                        ThermalPrinter.searchMark(obj[0], obj[1]);
                    } catch (TelpoException e6) {
                        e6.printStackTrace();
                    }
                    Printer.lock.notify();
                }
                return;
            } else if (msg.what == 5) {
                synchronized (Printer.lock) {
                    try {
                        ThermalPrinter.sendCommand((byte[]) msg.obj, msg.arg1);
                    } catch (TelpoException e7) {
                        e7.printStackTrace();
                    }
                    Printer.lock.notify();
                }
                return;
            } else {
                return;
            }
        }
    }

    public static synchronized int connect() {
        int ret;
        synchronized (Printer.class) {
            ret = 0;
            try {
                if (printList == null) {
                    printList = new ArrayList();
                }
                if (handlerThread == null) {
                    handlerThread = new HandlerThread(TAG);
                    handlerThread.start();
                    handler = new MyHandler(handlerThread.getLooper());
                }
                ThermalPrinter.start();
            } catch (DeviceAlreadyOpenException e) {
            } catch (TelpoException e2) {
                e2.printStackTrace();
                ret = -1;
            }
        }
        return ret;
    }

    public static synchronized void disconnect() {
        synchronized (Printer.class) {
            ThermalPrinter.stop();
            if (printList != null) {
                printList.clear();
                printList = null;
            }
            if (handlerThread != null) {
                handlerThread.quit();
                handlerThread = null;
                handler = null;
            }
            barcode_mode = -1;
            mVersion = null;
        }
    }

    public static synchronized void reset() {
        synchronized (Printer.class) {
            try {
                ThermalPrinter.reset();
            } catch (TelpoException e) {
                e.printStackTrace();
            }
            if (printList != null) {
                printList.clear();
            }
        }
        return;
    }

    public static synchronized void feedPaper(int lines) {
        synchronized (Printer.class) {
            if (lines > 0) {
                if (printList != null) {
                    PrintItem printItem = new PrintItem("", new StyleConfig());
                    printItem.feed = lines;
                    printList.add(printItem);
                }
            }
        }
    }

    public static synchronized int getStatus() {
        int i;
        synchronized (Printer.class) {
            synchronized (lock) {
                mStatus = STATUS_UNKNOWN;
                handler.sendEmptyMessage(1);
                try {
                    lock.wait(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            i = mStatus;
        }
        return i;
    }

    public static synchronized boolean isConnected() {
        boolean z;
        synchronized (Printer.class) {
            if (printList == null || handlerThread == null) {
                z = false;
            } else {
                z = true;
            }
        }
        return z;
    }

    public static synchronized void ledCtrl(int ledType, int onOff) {
        synchronized (Printer.class) {
            handler.sendMessage(handler.obtainMessage(2, ledType, onOff));
        }
    }

    public static synchronized String getVersion() {
        String str;
        synchronized (Printer.class) {
            if (mVersion == null) {
                synchronized (lock) {
                    handler.sendEmptyMessage(3);
                    try {
                        lock.wait(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            str = mVersion;
        }
        return str;
    }

    public static synchronized void searchMark(int search_disdance, int walk_disdance) {
        synchronized (Printer.class) {
            synchronized (lock) {
                handler.sendMessage(handler.obtainMessage(4, new int[]{search_disdance, walk_disdance}));
                try {
                    lock.wait(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized void sendCommand(byte[] cmd, int len) {
        synchronized (Printer.class) {
            synchronized (lock) {
                handler.sendMessage(handler.obtainMessage(5, len, len, cmd));
                try {
                    lock.wait(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static synchronized void sendCommand(String cmdStr) {
        synchronized (Printer.class) {
            if (cmdStr != null) {
                synchronized (lock) {
                    byte[] cmd = StringUtil.toBytes(cmdStr.replace(" ", ""));
                    handler.sendMessage(handler.obtainMessage(5, cmd.length, cmd.length, cmd));
                    try {
                        lock.wait(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return;
    }

    private static class CommitData {
        public ICommitCallback callback;
        public List<PrintItem> printList;

        private CommitData() {
            this.printList = null;
            this.callback = null;
        }

        /* synthetic */ CommitData(CommitData commitData) {
            this();
        }
    }

    private static Bitmap adjustBitmap(Bitmap bitmap, StyleConfig.Align align) {
        if (bitmap == null) {
            return null;
        }
        int adjustWidth = bitmap.getWidth();
        int adjustHeight = bitmap.getHeight();
        int offset = 0;
        if (align == StyleConfig.Align.CENTER) {
            offset = (384 - adjustWidth) / 2;
            adjustWidth += offset;
            int temp = adjustWidth % 8;
            if (temp != 0) {
                adjustWidth += 8 - temp;
            }
        } else if (align == StyleConfig.Align.RIGHT) {
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

    private static Bitmap CreateCode(String str, BarcodeFormat type, int bmpWidth, int bmpHeight) throws WriterException {
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
    }

    private static void printerReset() {
        try {
            ThermalPrinter.reset();
        } catch (TelpoException e) {
            e.printStackTrace();
        }
    }
}
