package com.telpo.tps550.api.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.Log;
import com.telpo.tps550.api.ErrorCode;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;

public class SY581ThermalPrinter extends ThermalPrinter {
    public static final int ALGIN_MIDDLE = 1;
    public static final int ALGIN_RIGHT = 2;
    public static final int ALIGN_LEFT = 0;
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
    private static final int PAPER_WIDTH = 576;
    private static final int RGB565_MASK_BLUE = 31;
    private static final int RGB565_MASK_GREEN = 2016;
    private static final int RGB565_MASK_RED = 63488;
    public static final int STATUS_CUT_ERROR = 4;
    public static final int STATUS_GATE_OPEN = 3;
    public static final int STATUS_NO_PAPER = 2;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OVER_HEAT = 1;
    public static final int STATUS_UNKNOWN = 5;
    private static final String TAG = "SY581ThermalPrinter";
    public static final int WALK_DOTLINE = 0;
    public static final int WALK_LINE = 1;
    private static int align_mode = 0;
    private static Bitmap bitmap = null;
    public static volatile byte buffull_flag = 0;
    private static StringBuilder builder = null;
    private static Canvas canvas = null;
    private static final int color = 128;
    private static int font_size = 2;
    private static int height_mult = 1;
    private static int indent = 0;
    private static int line_number;
    private static int line_space = ((font_size * 14) + 3);
    private static int origin_x;
    private static int origin_y;
    private static Paint paint;
    private static TextPaint textPaint;
    private static int width_mult = 1;
    public volatile byte buffull_flag1;

    public SY581ThermalPrinter(Context context) {
        super(context);
    }

    public static synchronized void reset() throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            initCanvas();
        }
    }

    public static synchronized int checkStatus() throws TelpoException {
        int i;
        synchronized (SY581ThermalPrinter.class) {
            switch (1) {
                case 0:
                    i = 0;
                    break;
                case ErrorCode.ERR_PRN_NO_PAPER:
                    i = 2;
                    break;
                case ErrorCode.ERR_PRN_OVER_TEMP:
                    i = 1;
                    break;
                case ErrorCode.ERR_PRN_GATE_OPEN:
                    i = 3;
                    break;
                case ErrorCode.ERR_PRN_NOT_CUT:
                    i = 4;
                    break;
                default:
                    i = 5;
                    break;
            }
        }
        return i;
    }

    public static synchronized void setAlign(int mode) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            align_mode = mode;
        }
    }

    public static synchronized void enlargeFontSize(int widthMultiple, int heightMultiple) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            width_mult = widthMultiple;
            height_mult = heightMultiple;
        }
    }

    public static synchronized void setFontSize(int type) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            if (type < 1 || type > 2) {
                throw new InternalErrorException();
            }
            font_size = type;
            line_space = (font_size * 14) + 3;
            textPaint.setTextSize((float) (font_size * 14));
            paint.setTextSize((float) (font_size * 14));
        }
    }

    public static synchronized void addString(String content) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            builder.append(content);
        }
    }

    public static synchronized void addBarcode(String barcode) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            byte[] bytes = barcode.getBytes();
        }
    }

    public static synchronized void clearString() throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            builder.delete(0, builder.length() - 1);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0074 A[Catch:{ Exception -> 0x00a5 }] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0060 A[Catch:{ Exception -> 0x00a5 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void printString() throws com.telpo.tps550.api.TelpoException {
        /*
            java.lang.Class<com.telpo.tps550.api.printer.SY581ThermalPrinter> r12 = com.telpo.tps550.api.printer.SY581ThermalPrinter.class
            monitor-enter(r12)
            r4 = 0
            int r1 = align_mode     // Catch:{ all -> 0x00aa }
            switch(r1) {
                case 0: goto L_0x0099;
                case 1: goto L_0x009d;
                case 2: goto L_0x00a1;
                default: goto L_0x0009;
            }     // Catch:{ all -> 0x00aa }
        L_0x0009:
            android.text.Layout$Alignment r4 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ all -> 0x00aa }
        L_0x000b:
            int r1 = indent     // Catch:{ all -> 0x00aa }
            origin_x = r1     // Catch:{ all -> 0x00aa }
            android.text.TextPaint r1 = textPaint     // Catch:{ all -> 0x00aa }
            float r1 = r1.getTextSize()     // Catch:{ all -> 0x00aa }
            int r11 = (int) r1     // Catch:{ all -> 0x00aa }
            android.text.TextPaint r1 = textPaint     // Catch:{ all -> 0x00aa }
            int r2 = width_mult     // Catch:{ all -> 0x00aa }
            int r2 = r2 * r11
            float r2 = (float) r2     // Catch:{ all -> 0x00aa }
            r1.setTextSize(r2)     // Catch:{ all -> 0x00aa }
            android.text.StaticLayout r0 = new android.text.StaticLayout     // Catch:{ all -> 0x00aa }
            java.lang.StringBuilder r1 = builder     // Catch:{ all -> 0x00aa }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00aa }
            android.text.TextPaint r2 = textPaint     // Catch:{ all -> 0x00aa }
            int r3 = origin_x     // Catch:{ all -> 0x00aa }
            int r3 = 576 - r3
            r5 = 1065353216(0x3f800000, float:1.0)
            r6 = 0
            r7 = 1
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)     // Catch:{ all -> 0x00aa }
            android.graphics.Canvas r1 = canvas     // Catch:{ all -> 0x00aa }
            int r2 = origin_x     // Catch:{ all -> 0x00aa }
            float r2 = (float) r2     // Catch:{ all -> 0x00aa }
            int r3 = origin_y     // Catch:{ all -> 0x00aa }
            float r3 = (float) r3     // Catch:{ all -> 0x00aa }
            r1.translate(r2, r3)     // Catch:{ all -> 0x00aa }
            int r1 = origin_y     // Catch:{ all -> 0x00aa }
            int r2 = r0.getHeight()     // Catch:{ all -> 0x00aa }
            int r1 = r1 + r2
            origin_y = r1     // Catch:{ all -> 0x00aa }
            android.graphics.Canvas r1 = canvas     // Catch:{ all -> 0x00aa }
            r0.draw(r1)     // Catch:{ all -> 0x00aa }
            android.text.TextPaint r1 = textPaint     // Catch:{ all -> 0x00aa }
            float r2 = (float) r11     // Catch:{ all -> 0x00aa }
            r1.setTextSize(r2)     // Catch:{ all -> 0x00aa }
            java.io.File r9 = new java.io.File     // Catch:{ all -> 0x00aa }
            java.lang.String r1 = "/sdcard/ttt.png"
            r9.<init>(r1)     // Catch:{ all -> 0x00aa }
            boolean r1 = r9.exists()     // Catch:{ all -> 0x00aa }
            if (r1 == 0) goto L_0x0063
            r9.delete()     // Catch:{ all -> 0x00aa }
        L_0x0063:
            java.io.FileOutputStream r10 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x00a5 }
            r10.<init>(r9)     // Catch:{ Exception -> 0x00a5 }
            android.graphics.Bitmap r1 = bitmap     // Catch:{ Exception -> 0x00a5 }
            android.graphics.Bitmap$CompressFormat r2 = android.graphics.Bitmap.CompressFormat.PNG     // Catch:{ Exception -> 0x00a5 }
            r3 = 100
            boolean r1 = r1.compress(r2, r3, r10)     // Catch:{ Exception -> 0x00a5 }
            if (r1 == 0) goto L_0x007a
            r10.flush()     // Catch:{ Exception -> 0x00a5 }
            r10.close()     // Catch:{ Exception -> 0x00a5 }
        L_0x007a:
            java.lang.String r1 = "SY581ThermalPrinter"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00aa }
            java.lang.String r3 = "image height = "
            r2.<init>(r3)     // Catch:{ all -> 0x00aa }
            int r3 = origin_y     // Catch:{ all -> 0x00aa }
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x00aa }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00aa }
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x00aa }
            android.graphics.Bitmap r1 = bitmap     // Catch:{ all -> 0x00aa }
            int r2 = origin_y     // Catch:{ all -> 0x00aa }
            printLogoWithinHeight(r1, r2)     // Catch:{ all -> 0x00aa }
            monitor-exit(r12)
            return
        L_0x0099:
            android.text.Layout$Alignment r4 = android.text.Layout.Alignment.ALIGN_NORMAL     // Catch:{ all -> 0x00aa }
            goto L_0x000b
        L_0x009d:
            android.text.Layout$Alignment r4 = android.text.Layout.Alignment.ALIGN_CENTER     // Catch:{ all -> 0x00aa }
            goto L_0x000b
        L_0x00a1:
            android.text.Layout$Alignment r4 = android.text.Layout.Alignment.ALIGN_OPPOSITE     // Catch:{ all -> 0x00aa }
            goto L_0x000b
        L_0x00a5:
            r8 = move-exception
            r8.printStackTrace()     // Catch:{ all -> 0x00aa }
            goto L_0x007a
        L_0x00aa:
            r1 = move-exception
            monitor-exit(r12)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.SY581ThermalPrinter.printString():void");
    }

    public static synchronized void printStringAndWalk(int direction, int mode, int lines) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
        }
    }

    public static synchronized void setLineSpace(int lineSpace) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            line_space = lineSpace;
        }
    }

    public static synchronized void setLeftIndent(int space) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            indent = space;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0013, code lost:
        if ((r7 % 8) != 0) goto L_0x0015;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0026, code lost:
        if ((r8 % 8) != 0) goto L_0x0028;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void printLogo(int r7, int r8, char[] r9) throws com.telpo.tps550.api.TelpoException {
        /*
            java.lang.Class<com.telpo.tps550.api.printer.SY581ThermalPrinter> r5 = com.telpo.tps550.api.printer.SY581ThermalPrinter.class
            monitor-enter(r5)
            r2 = 1
            r4 = 3
            if (r2 == r4) goto L_0x000d
            r4 = 4
            if (r2 == r4) goto L_0x000d
            r4 = 5
            if (r2 != r4) goto L_0x0020
        L_0x000d:
            r4 = 576(0x240, float:8.07E-43)
            if (r7 > r4) goto L_0x0015
            int r4 = r7 % 8
            if (r4 == 0) goto L_0x0030
        L_0x0015:
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x001d }
            java.lang.String r6 = "The width of the image to print is illegal!"
            r4.<init>(r6)     // Catch:{ all -> 0x001d }
            throw r4     // Catch:{ all -> 0x001d }
        L_0x001d:
            r4 = move-exception
            monitor-exit(r5)
            throw r4
        L_0x0020:
            r4 = 384(0x180, float:5.38E-43)
            if (r7 > r4) goto L_0x0028
            int r4 = r8 % 8
            if (r4 == 0) goto L_0x0030
        L_0x0028:
            java.lang.IllegalArgumentException r4 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x001d }
            java.lang.String r6 = "The width or the height of the image to print is illegal!"
            r4.<init>(r6)     // Catch:{ all -> 0x001d }
            throw r4     // Catch:{ all -> 0x001d }
        L_0x0030:
            int r4 = r9.length     // Catch:{ all -> 0x001d }
            byte[] r1 = new byte[r4]     // Catch:{ all -> 0x001d }
            r0 = 0
        L_0x0034:
            int r4 = r9.length     // Catch:{ all -> 0x001d }
            if (r0 < r4) goto L_0x003a
            r3 = 1
            monitor-exit(r5)
            return
        L_0x003a:
            char r4 = r9[r0]     // Catch:{ all -> 0x001d }
            byte r4 = (byte) r4     // Catch:{ all -> 0x001d }
            r1[r0] = r4     // Catch:{ all -> 0x001d }
            int r0 = r0 + 1
            goto L_0x0034
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.printer.SY581ThermalPrinter.printLogo(int, int, char[]):void");
    }

    public static synchronized void printLogo(Bitmap image) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            if (image == null) {
                throw new NullPointerException();
            }
            int height = image.getHeight();
            int width = image.getWidth();
            int image_row_bytes = ((width - 1) >> 3) + 1;
            int pos = 0;
            byte[] Imagelogo = new byte[(image_row_bytes * height)];
            int row_index = 0;
            while (row_index < height) {
                int col_index = 0;
                int h = 0;
                int pos2 = pos;
                while (col_index < image_row_bytes) {
                    int temp = 0;
                    int i = 0;
                    while (true) {
                        if (i >= 8) {
                            break;
                        } else if (i + h >= width) {
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
                    int pos3 = pos2 + 1;
                    Imagelogo[pos2] = (byte) temp;
                    col_index++;
                    h += 8;
                    pos2 = pos3;
                }
                row_index++;
                pos = pos2;
            }
            Log.d(TAG, "width = " + width + ",image_row_bytes = " + image_row_bytes + ",height = " + height);
            Log.d(TAG, "data length = " + Imagelogo.length);
        }
    }

    public static synchronized void printLogo(Bitmap image, int mode) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            if (image == null) {
                throw new NullPointerException();
            }
            int height = image.getHeight();
            int width = image.getWidth();
            int image_row_bytes = ((width - 1) >> 3) + 1;
            int pos = 0;
            byte[] Imagelogo = new byte[(image.getHeight() * image_row_bytes)];
            Log.d(TAG, "width = " + width + ",image_row_bytes = " + image_row_bytes);
            int row_index = 0;
            while (row_index < height) {
                int col_index = 0;
                int h = 0;
                int pos2 = pos;
                while (col_index < width) {
                    int temp = 0;
                    int i = 0;
                    while (true) {
                        if (i >= 8) {
                            break;
                        } else if (i + h >= width) {
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
                    int pos3 = pos2 + 1;
                    Imagelogo[pos2] = (byte) temp;
                    col_index++;
                    h += 8;
                    pos2 = pos3;
                }
                row_index++;
                pos = pos2;
            }
        }
    }

    public static void paperCut() throws TelpoException {
    }

    public static void setBold(boolean isBold) throws TelpoException {
        textPaint.setFakeBoldText(isBold);
        paint.setFakeBoldText(isBold);
    }

    private static void initCanvas() {
        align_mode = 0;
        indent = 0;
        font_size = 2;
        line_space = (font_size * 14) + 3;
        width_mult = 1;
        height_mult = 1;
        bitmap = Bitmap.createBitmap(PAPER_WIDTH, 3000, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        textPaint = new TextPaint();
        paint = new Paint();
        canvas.drawColor(-1);
        textPaint.setColor(-16777216);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize((float) (font_size * 14));
        textPaint.setFakeBoldText(false);
        origin_y = 0;
        origin_x = 0;
        paint.setColor(-16777216);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(8.0f);
        builder = new StringBuilder();
    }

    private static synchronized void printLogoWithinHeight(Bitmap image, int height) throws TelpoException {
        synchronized (SY581ThermalPrinter.class) {
            if (image == null) {
                throw new NullPointerException();
            }
            int width = image.getWidth();
            int image_row_bytes = ((width - 1) >> 3) + 1;
            int pos = 0;
            byte[] Imagelogo = new byte[(image_row_bytes * height)];
            Log.d(TAG, "print logo width = " + width + ",image_row_bytes = " + image_row_bytes + ",height = " + height);
            int row_index = 0;
            while (row_index < height) {
                int col_index = 0;
                int h = 0;
                int pos2 = pos;
                while (col_index < image_row_bytes) {
                    int temp = 0;
                    int i = 0;
                    while (true) {
                        if (i >= 8) {
                            break;
                        } else if (i + h >= width) {
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
                    int pos3 = pos2 + 1;
                    Imagelogo[pos2] = (byte) temp;
                    col_index++;
                    h += 8;
                    pos2 = pos3;
                }
                row_index++;
                pos = pos2;
            }
        }
    }

    public static String byte2HexString(byte[] data) {
        if (data == null) {
            return "";
        }
        return byte2HexString(data, 0, data.length);
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
}
