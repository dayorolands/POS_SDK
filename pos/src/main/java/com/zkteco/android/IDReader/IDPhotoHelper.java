package com.zkteco.android.IDReader;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class IDPhotoHelper {
    public static Bitmap Bgr2Bitmap(byte[] bgrbuf) {
        int width = WLTService.imgWidth;
        Bitmap bmp = Bitmap.createBitmap(width, WLTService.imgHeight, Bitmap.Config.RGB_565);
        int row = 0;
        int col = width - 1;
        int i = bgrbuf.length - 1;
        while (true) {
            int col2 = col;
            if (i < 3) {
                return bmp;
            }
            col = col2 - 1;
            bmp.setPixel(col2, row, (bgrbuf[i] & 255) + ((bgrbuf[i - 1] << 8) & 65280) + ((bgrbuf[i - 2] << 16) & 16711680));
            if (col < 0) {
                col = width - 1;
                row++;
            }
            i -= 3;
        }
    }

    public static Bitmap createMyBitmap(byte[] data, int width, int height) {
        int[] colors = convertByteToColor(data);
        if (colors == null) {
            return null;
        }
        try {
            return Bitmap.createBitmap(colors, 0, width, width, height, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            return null;
        }
    }

    private static int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }
        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }
        int[] color = new int[((size / 3) + arg)];
        if (arg == 0) {
            for (int i = 0; i < color.length; i++) {
                color[i] = ((data[i * 3] << 16) & 16711680) | ((data[(i * 3) + 1] << 8) & 65280) | (data[(i * 3) + 2] & 255) | -16777216;
            }
            return color;
        }
        for (int i2 = 0; i2 < color.length - 1; i2++) {
            color[i2] = ((data[i2 * 3] << 16) & 16711680) | ((data[(i2 * 3) + 1] << 8) & 65280) | (data[(i2 * 3) + 2] & 255) | -16777216;
        }
        color[color.length - 1] = -16777216;
        return color;
    }

    private static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }
}
